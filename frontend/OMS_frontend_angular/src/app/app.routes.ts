import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { Navbar } from './shared/components/navbar/navbar';
import { DashboardComponent } from '../feature/admin/pages/dashboard/dashboard.component';

export const routes: Routes = [
    {
        path : "",
        component: Login,
        pathMatch : "full"
    },
    // {
    //     path: '',
    //     component : DashboardComponent,
    //     pathMatch : "full"
    // },
    // temporary route
    // {
    //     path : "",
    //     component: Navbar,
    //     pathMatch : "full"
    // }
];
