import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user/user.service';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-dashboard',
  imports: [BaseChartDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  constructor(private userService: UserService) {}

  role: string = '';

  ngOnInit(): void {
    let role = this.userService.getRole();
    this.role = this.treatRoleName(role);
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
    labels: ['Pending', 'Completed', 'Cancelled'],
    datasets: [
      {
        data: [12, 30, 5],
        backgroundColor: ['#facc15', '#22c55e', '#ef4444'],
      },
    ],
  };

  pieChartType: 'pie' = 'pie';

  // 📊 BAR CHART (Monthly Orders)
  barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr'],
    datasets: [
      {
        label: 'Orders',
        data: [20, 35, 50, 25],
        backgroundColor: '#6366f1',
      },
    ],
  };

  barChartType: 'bar' = 'bar';
}
