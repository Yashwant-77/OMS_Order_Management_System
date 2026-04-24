import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { UserService } from '../../../services/user/user.service';
import { DashboardApiService } from '../../services/dashboard-api.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, BaseChartDirective, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  constructor(
    private userService: UserService,
    private dashboardApiService: DashboardApiService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {}

  role: string = '';
  totalOrders: number = 0;
  totalCustomers: number = 0;
  totalBills: number = 0;
  totalOrderValue = 0;

  ngOnInit(): void {
    const role = this.userService.getRole();
    this.role = this.treatRoleName(role);

    this.getDashboardSummary();
  }

  getDashboardSummary() {
    this.dashboardApiService.getDashboardSummary().subscribe({
      next: (res: any) => {
        console.log(res);

        this.totalOrders = res.totalOrders;
        this.totalOrderValue = res.totalOrderValue;
        this.totalCustomers = res.totalCustomers;
        this.totalBills = res.totalBills;

        this.pieChartData = {
          labels: ['Pending', 'Confirmed', 'Cancelled', 'Shipped', 'Delivered', 'Processing'],
          datasets: [
            {
              data: [
                res.pendingOrders,
                res.confirmedOrders,
                res.cancelledOrders,
                res.shippedOrders,
                res.deliveredOrders,
                res.processingOrders,
              ],
              backgroundColor: ['#facc15', '#22c55e', '#ef4444', '#3b82f6', '#10b981', '#a855f7'],
            },
          ],
        };

        if (res.monthlyOrders && res.monthlyOrders.length > 0) {
          this.barChartData = {
            labels: res.monthlyOrders.map((m: any) => m.month),
            datasets: [
              {
                label: 'Orders',
                data: res.monthlyOrders.map((m: any) => m.count),
                backgroundColor: '#6366f1',
              },
            ],
          };
        }

        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.log('Status code : ', err.status);
        console.error('Error loading dashboard summary', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  formatAmount(value: number): string {
    if (value >= 10000000) {
      return (value / 10000000).toFixed(1).replace(/\.0$/, '') + 'Cr';
    } else if (value >= 100000) {
      return (value / 100000).toFixed(1).replace(/\.0$/, '') + 'L';
    } else if (value >= 1000) {
      return (value / 1000).toFixed(1).replace(/\.0$/, '') + 'K';
    } else {
      return value.toString();
    }
  }

  treatRoleName(role: string): string {
    return role
      .toLowerCase()
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [],
      },
    ],
  };

  pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
  };

  pieChartType: 'pie' = 'pie';

  barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        label: 'Orders',
        data: [],
        backgroundColor: '#6366f1',
      },
    ],
  };

  barChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
  };

  barChartType: 'bar' = 'bar';
}
