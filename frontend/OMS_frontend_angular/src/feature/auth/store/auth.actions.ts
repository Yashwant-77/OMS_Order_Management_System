
// import { createAction } from "@ngrx/store";


// export const loginSuccess = createAction('[Login Component] LoginSuccess');
// export const logout = createAction('[Login Component] logout')











import { createAction, props } from '@ngrx/store';

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ user: any; token: string }>()
);

export const logout = createAction('[Auth] Logout');