import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { HomeLayout } from './layouts/home-layout/home-layout';


export const routes: Routes = [
    {
        path : "login",
        component :Login,
        pathMatch : "full"
    },
    {
        path : "",
        component: HomeLayout,
        pathMatch : "full"
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
