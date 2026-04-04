import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import * as AuthActions from './auth.actions';
import { AuthApi } from '../../services/api/auth.api';
import { login, loginSuccess, loginFailure, logout } from './auth.actions';
import { catchError, map, switchMap, of, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable()
export class AuthEffects {

  constructor(
    private actions$: Actions,
    private authApi: AuthApi,
    private router: Router
  ) {}

  // 🔹 LOGIN EFFECT (API CALL HAPPENS HERE)
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(login),
      switchMap((action) =>
        this.authApi.callLogin(action.credential).pipe(
          map((res: any) => {
            // Save token in localStorage
            localStorage.setItem('token', res.token);
            localStorage.setItem('user', JSON.stringify(res.user));

            return loginSuccess({
              user: res.user,
              token: res.token
            });
          }),
          catchError((error) => {
            console.error('Login failed', error);
            return of(loginFailure());
          })
        )
      )
    )
  );

  // 🔹 LOGIN SUCCESS → REDIRECT BASED ON ROLE
  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(loginSuccess),
        tap((action) => {
          const role = action.user.role;

          if (role === 'ADMINISTRATOR') {
            this.router.navigate(['/admin']);
          } else if (role === 'PRODUCT_MANAGER') {
            this.router.navigate(['/product']);
          } else if (role === 'SALES_REPRESENTATIVE') {
            this.router.navigate(['/sales']);
          } else if (role === 'PURCHASING_OFFICER') {
            this.router.navigate(['/purchase']);
          } else if (role === 'FINANCE_MANAGER') {
            this.router.navigate(['/finance']);
          } else {
            this.router.navigate(['/']);
          }
        })
      ),
    { dispatch: false } // because we are not dispatching new action
  );

  // 🔹 LOGOUT EFFECT
  logout$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(logout),
        tap(() => {
          // Clear storage
          localStorage.removeItem('token');
          localStorage.removeItem('user');

          // Redirect to login
          this.router.navigate(['/login']);
        })
      ),
    { dispatch: false }
  );
}