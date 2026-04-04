import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.state';

export const selectAuthState =
  createFeatureSelector<AuthState>('auth');

export const selectUser = createSelector(
  selectAuthState,
  (state) => state.user
);

export const selectRole = createSelector(
  selectUser,
  (user) => user?.role
);

export const selectEmail = createSelector(
  selectUser,
  (user) => user?.email
);