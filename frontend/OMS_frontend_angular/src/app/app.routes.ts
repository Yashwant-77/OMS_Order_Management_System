import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { HomeLayout } from './layouts/home-layout/home-layout';
import { authGuard } from './guards/auth-guard';
import { Orders } from '../feature/orders/components/orders/orders';


export const routes: Routes = [
    {
        path : "login",
        component :Login,
        pathMatch : "full"
    },
    {
        path : "",
        component: HomeLayout,
        canActivate : [authGuard],
        children: [
            { path: 'admin', component: Orders , canActivate : [authGuard] , data : { role : ["ADMINISTRATOR" , "SALES_REPRESENTATIVE"]} },
             { path: '', redirectTo: 'admin', pathMatch: 'full' }
        ]
    }

]