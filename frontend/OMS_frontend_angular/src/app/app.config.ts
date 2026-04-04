import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideStore } from '@ngrx/store';
import { authReducer } from '../core/store/auth/auth.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideStore({
      auth : authReducer
    })
]
};
