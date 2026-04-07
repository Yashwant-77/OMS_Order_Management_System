import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UserService } from '../services/user/user.service';

export const authGuard: CanActivateFn = (route, state) => {
  
  const userService = inject(UserService);
  const router = inject(Router);

  if(userService.getToken() !== null || userService.getToken() !== ""){

    const role = route.data['role'] as String;
    if(role){
      const match = userService.roleMatch(role)
      if(match){
        return true;
      }
      else {
        router.navigate(['/forbidden'])
        return false;
      }
    }

  }

  router.navigate(['/login'])
  return false;
};
