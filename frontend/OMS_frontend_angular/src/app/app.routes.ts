import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { HomeLayout } from './layouts/home-layout/home-layout';
import { authGuard } from './guards/auth-guard';


export const routes: Routes = [
    {
        path : "login",
        component :Login,
        pathMatch : "full"
    },
    {
        path : "",
        component: HomeLayout,
        pathMatch : "full",
        children: [
            // { path: 'admin', canActivate:[authGuard] 
                // ,component: 
                //  ,data : {role:["ADMINISTRATOR"]}},
            // { path: 'bom', component: OrdersComponent },
            // { path: 'orders', component: BomComponent }
            // { path: 'purchase', component: BomComponent }
            // { path: 'invoice', component: OrdersComponent },
            // { path: 'reports', component: BomComponent }
        ]
    }







    // Public route
//   { path: 'login', component: LoginComponent },

  // Layout route
//   {
//     path: '',
//     component: LayoutComponent,
//     canActivate: [AuthGuard],
//     children: [
//       { path: 'orders', component: OrdersComponent },
//       { path: 'bom', component: BomComponent },
//       { path: 'purchase', component: PurchaseComponent },
//       { path: 'invoice', component: InvoiceComponent },
//       { path: 'reports', component: ReportsComponent },

//       // default route
//       { path: '', redirectTo: 'orders', pathMatch: 'full' }
//     ]
//   },

//   // fallback
//   { path: '**', redirectTo: '' }
];
