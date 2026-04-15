package OMS_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Manufacturing OMS API")
                        .description(
                                "Web-Based Order Management System for " +
                                        "ABC Power Equipment Inc. — manages orders, " +
                                        "Bills of Materials, purchase orders, invoices, payments, " +
                                        "and reporting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("OMS Backend Team")
                                .email("john@abclimited.com")))

                // Tell Swagger this API uses Bearer token auth
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("Bearer Authentication")));
    }
}