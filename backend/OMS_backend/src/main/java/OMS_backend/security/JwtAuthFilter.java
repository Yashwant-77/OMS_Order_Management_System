package OMS_backend.security;

import OMS_backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Read the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If header is missing or doesn't start with "Bearer ", skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (everything after "Bearer ")
        final String token = authHeader.substring(7);

        try {


            // Step 4: Extract username from token
            final String username = jwtUtil.extractUsername(token);

            // Step 5: If username exists and no authentication is set yet in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load user from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 7: Validate token against loaded user
                if (jwtUtil.isTokenValid(token, userDetails)) {

                    // Step 8: Create authentication object and set it in SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,              // credentials null → already authenticated
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 9: Mark this request as authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch(io.jsonwebtoken.ExpiredJwtException e){
            // 🔥 THIS IS THE FIX
            SecurityContextHolder.clearContext(); // 🔥 VERY IMPORTANT
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");

            return;
        }

        // Step 10: Continue the filter chain
        filterChain.doFilter(request, response);
    }
}