import { createReducer, on } from '@ngrx/store';
import { loginSuccess, logout } from './auth.actions';
import { initialState } from './auth.state';

export const authReducer = createReducer(
  initialState,

  on(loginSuccess, (state, { user, token }) => ({
    ...state,
    user,
    token
  })),

  on(logout, () => initialState)
);