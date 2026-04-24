import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../services/user/user.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const userService = inject(UserService);
  const router = inject(Router);

  // Skip token for No-Auth requests
  if (req.headers.get('No-Auth') === 'True') {
    return next(req);
  }

  const token = userService.getToken();

  let modifiedReq = req;

  if (token) {
    modifiedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(modifiedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      console.log(err.status);
      console.log(err);

      if (err.status === 401) {
        router.navigate(['/login']);
      } else if (err.status === 403) {
        router.navigate(['/forbidden']);
      }

      return throwError(() => new Error('Something is wrong'));
    })
  );
};