import { Routes } from '@angular/router';
import { Login } from '../feature/auth/pages/login/login';
import { HomeLayout } from './layouts/home-layout/home-layout';
import { authGuard } from './guards/auth-guard';
import { Orders } from '../feature/orders/components/view-orders/view-orders';
import { Forbidden } from './shared/components/forbidden/forbidden';
import { CreateOrder } from '../feature/orders/components/create-order/create-order';
import { Dashboard } from './shared/components/dashboard/dashboard';
import { Invoice } from '../feature/invoice/components/invoice/invoice';
import { Purchase } from '../feature/purchase-orders/components/purchase/purchase';
import { Reports } from '../feature/reports/components/reports/reports';
import { Users } from '../feature/users/components/users/users';
import { Profile } from './shared/components/profile/profile';
import { BomCreate } from '../feature/bom/components/bom-create/bom-create';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    pathMatch: 'full',
  },

  {
    path: '',
    component: HomeLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'forbidden',
        component: Forbidden,
        pathMatch: 'full',
      },
      {
        path: 'profile',
        component: Profile,
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        component: Dashboard,
        canActivate: [authGuard],
        data : { role : ['ADMINISTRATOR', 'SALES_REPRESENTATIVE' , 'PRODUCT_MANAGER' , 'FINANCE_MANAGER' , 'BUSINESS_ANALYST' ,'PURCHASING_OFFICER']}
      },
      {
        path: 'sales-orders',
        component: Orders,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR', 'SALES_REPRESENTATIVE'] },
      },
      {
        path: 'edit-order/:id',
        component: CreateOrder,
        canActivate: [authGuard],
        data: { role: ['SALES_REPRESENTATIVE'] },
      },
      {
        path: 'bom',
        component: BomCreate,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR', 'PRODUCT_MANAGER' , 'SALES_REPRESENTATIVE'] },
      },
      {
        path: 'invoice',
        component: Invoice,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR', 'FINANCE_MANAGER'] },
      },
      {
        path: 'purchase-orders',
        component: Purchase,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR', 'PURCHASING_OFFICER'] },
      },
      {
        path: 'reports',
        component: Reports,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR', 'BUSINESS_ANALYST'] },
      },
      {
        path: 'users',
        component: Users,
        canActivate: [authGuard],
        data: { role: ['ADMINISTRATOR'] },
      },
      {
        path: 'add-product',
        component: CreateOrder,
        canActivate: [authGuard],
        data: { role: [ 'SALES_REPRESENTATIVE'] },
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
