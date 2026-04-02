import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Navbar } from './components/navbar/navbar';

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
