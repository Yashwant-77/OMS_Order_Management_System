import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UserService } from '../services/user/user.service';

export const authGuard: CanActivateFn = (route, state) => {
  
  const userService = inject(UserService);
  const router = inject(Router);
  const token = userService.getToken();

  if(token && token !== ""){

    const roles = route.data['role'] as string[];
    if(roles && roles.length > 0){
      const match = userService.roleMatch(roles)
      if(match){
        return true;
      }
      else {
        router.navigate(['/forbidden'])
        return false;
      }
    }
    // If no specific role requirement, allow access for authenticated users
    return true;
  }

  router.navigate(['/login'])
  return false;
};
