import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { Navbar } from './shared/components/navbar/navbar';

export const routes: Routes = [
    // {
    //     path : "",
    //     component: Login,
    //     pathMatch : "full"
    // },
    // temporary route
    {
        path : "",
        component: Navbar,
        pathMatch : "full"
    }
];
