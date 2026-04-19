import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user/user.service';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { DashboardApiService } from '../../services/dashboard-api.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, BaseChartDirective , RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  constructor(
    private userService: UserService,
    private dashboardApiService: DashboardApiService,
    private cdr: ChangeDetectorRef,
    private router : Router
  ) {}

  role: string = '';
  totalOrders: number = 0;
  totalCustomers: number = 0;
  totalBills: number = 0;
  totalOrderValue = 0;

  ngOnInit(): void {
    let role = this.userService.getRole();
    this.role = this.treatRoleName(role);

    this.getPieChartData();
    this.getBarGraphData();
  }

  getPieChartData() {
    this.dashboardApiService.getPieChartData().subscribe({
      next: (res: any) => {
        console.log(res);

        this.totalOrders = res.totalOrders;
        this.totalOrderValue = res.totalOrderValue;

        // 🔥 UPDATE PIE CHART HERE
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
              backgroundColor: [
                '#facc15', // Pending
                '#22c55e', // Confirmed
                '#ef4444', // Cancelled
                '#3b82f6', // Shipped
                '#10b981', // Delivered
                '#a855f7', // Processing
              ],
            },
          ],
        };

        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.log('Status code : ', err.status);
        console.error('Error in deleting order !', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  getBarGraphData() {
  this.dashboardApiService.getBarGraphData().subscribe({
    next: (res: any) => {
      console.log(res);

      // cards
      this.totalCustomers = res.totalCustomers;
      this.totalBills = res.totalBills;

      // 🔥 BAR CHART UPDATE
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

      this.cdr.detectChanges(); // needed sometimes
    },
    error: (err) => {
      console.log('Status code : ', err.status);
        console.error('Error in deleting order !', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
    },
  });
}

  formatAmount(value: number): string {
    if (value >= 10000000) {
      // Crore
      return (value / 10000000).toFixed(1).replace(/\.0$/, '') + 'Cr';
    } else if (value >= 100000) {
      // Lakh
      return (value / 100000).toFixed(1).replace(/\.0$/, '') + 'L';
    } else if (value >= 1000) {
      // Thousand (optional)
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

  // 📊 PIE CHART (Order Status)

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

  // 📊 BAR CHART (Monthly Orders)
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

  barChartType: 'bar' = 'bar';
}
