import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { forkJoin } from 'rxjs';
import {
  CustomerOrderReport,
  LowStockReport,
  MonthlyOrderTrendReport,
  MonthlyPaymentTrendReport,
  OrderSummaryReport,
  PaymentSummaryReport,
  ReportsApiService,
  RevenueReport,
} from '../../services/reports-api.service';

@Component({
  selector: 'app-reports',
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports implements OnInit {
  constructor(private reportsApiService: ReportsApiService , private cdr : ChangeDetectorRef) {}

  year: number = new Date().getFullYear();
  loading = true;
  errorMessage = '';

  orderSummary: OrderSummaryReport | null = null;
  revenueSummary: RevenueReport | null = null;
  lowStockCount = 0;

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      orderSummary: this.reportsApiService.getOrderSummary(),
      revenueSummary: this.reportsApiService.getRevenueReport(),
      paymentSummary: this.reportsApiService.getPaymentSummary(),
      customerOrders: this.reportsApiService.getCustomerOrderReport(),
      lowStock: this.reportsApiService.getLowStockReport(15),
      monthlyOrderTrend: this.reportsApiService.getMonthlyOrderTrend(this.year),
      monthlyPaymentTrend: this.reportsApiService.getMonthlyPaymentTrend(this.year),
    }).subscribe({
      next: (res) => {
        this.orderSummary = res.orderSummary;
        this.revenueSummary = res.revenueSummary;
        this.lowStockCount = res.lowStock.length;

        this.buildOrderStatusChart(res.orderSummary);
        this.buildInvoiceStatusChart(res.revenueSummary);
        this.buildPaymentMethodChart(res.paymentSummary);
        this.buildCustomerValueChart(res.customerOrders);
        this.buildLowStockChart(res.lowStock);
        this.buildMonthlyTrendChart(res.monthlyOrderTrend, res.monthlyPaymentTrend);

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Unable to load reports right now. Please try again.';
      },
    });
  }

  private buildOrderStatusChart(summary: OrderSummaryReport): void {
    this.orderStatusChartData = {
      labels: ['Pending', 'Confirmed', 'Processing', 'Shipped', 'Delivered', 'Cancelled'],
      datasets: [
        {
          data: [
            summary.pendingOrders,
            summary.confirmedOrders,
            summary.processingOrders,
            summary.shippedOrders,
            summary.deliveredOrders,
            summary.cancelledOrders,
          ],
          backgroundColor: ['#F59E0B', '#10B981', '#6366F1', '#3B82F6', '#14B8A6', '#EF4444'],
        },
      ],
    };
  }

  private buildInvoiceStatusChart(summary: RevenueReport): void {
    this.invoiceStatusChartData = {
      labels: ['Paid', 'Partially Paid', 'Pending'],
      datasets: [
        {
          data: [summary.paidInvoices, summary.partiallyPaidInvoices, summary.pendingInvoices],
          backgroundColor: ['#10B981', '#F59E0B', '#EF4444'],
        },
      ],
    };
  }

  private buildPaymentMethodChart(paymentSummary: PaymentSummaryReport[]): void {
    this.paymentMethodChartData = {
      labels: paymentSummary.map((item) => this.prettyLabel(item.paymentMethod)),
      datasets: [
        {
          data: paymentSummary.map((item) => item.totalAmount),
          backgroundColor: ['#0EA5E9', '#8B5CF6', '#22C55E', '#FB7185', '#F97316'],
        },
      ],
    };
  }

  private buildCustomerValueChart(customerOrders: CustomerOrderReport[]): void {
    const topCustomers = [...customerOrders]
      .sort((a, b) => b.totalOrderValue - a.totalOrderValue)
      .slice(0, 8);

    this.customerValueChartData = {
      labels: topCustomers.map((item) => item.customerName),
      datasets: [
        {
          label: 'Order Value',
          data: topCustomers.map((item) => item.totalOrderValue),
          backgroundColor: '#2563EB',
          borderRadius: 6,
        },
      ],
    };
  }

  private buildLowStockChart(lowStock: LowStockReport[]): void {
    const lowestStockProducts = [...lowStock]
      .sort((a, b) => a.quantityInStock - b.quantityInStock)
      .slice(0, 10);

    this.lowStockChartData = {
      labels: lowestStockProducts.map((item) => item.productName),
      datasets: [
        {
          label: 'Stock Quantity',
          data: lowestStockProducts.map((item) => item.quantityInStock),
          backgroundColor: '#F97316',
        },
      ],
    };
  }

  private buildMonthlyTrendChart(
    monthlyOrders: MonthlyOrderTrendReport[],
    monthlyPayments: MonthlyPaymentTrendReport[],
  ): void {
    const paymentByMonth = new Map(monthlyPayments.map((item) => [item.monthNumber, item]));

    this.monthlyTrendChartData = {
      labels: monthlyOrders.map((item) => item.month),
      datasets: [
        {
          label: 'Orders Count',
          data: monthlyOrders.map((item) => item.orderCount),
          borderColor: '#6366F1',
          backgroundColor: 'rgba(99,102,241,0.2)',
          yAxisID: 'y',
          tension: 0.35,
          fill: true,
        },
        {
          label: 'Order Value',
          data: monthlyOrders.map((item) => item.totalOrderValue),
          borderColor: '#10B981',
          backgroundColor: 'rgba(16,185,129,0.2)',
          yAxisID: 'y1',
          tension: 0.35,
        },
        {
          label: 'Payments Collected',
          data: monthlyOrders.map(
            (item) => paymentByMonth.get(item.monthNumber)?.totalPaymentAmount ?? 0,
          ),
          borderColor: '#F97316',
          backgroundColor: 'rgba(249,115,22,0.2)',
          yAxisID: 'y1',
          tension: 0.35,
        },
      ],
    };
  }

  private prettyLabel(value: string): string {
    return value
      .split('_')
      .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
      .join(' ');
  }

  formatAmount(value: number | undefined): string {
    const amount = value ?? 0;
    return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(amount);
  }

  orderStatusChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [],
    datasets: [{ data: [] }],
  };
  orderStatusChartType: 'doughnut' = 'doughnut';
  orderStatusChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
  };

  invoiceStatusChartData: ChartConfiguration<'polarArea'>['data'] = {
    labels: [],
    datasets: [{ data: [] }],
  };
  invoiceStatusChartType: 'polarArea' = 'polarArea';
  invoiceStatusChartOptions: ChartOptions<'polarArea'> = {
    responsive: true,
    maintainAspectRatio: false,
  };

  paymentMethodChartData: ChartConfiguration<'pie'>['data'] = {
    labels: [],
    datasets: [{ data: [] }],
  };
  paymentMethodChartType: 'pie' = 'pie';
  paymentMethodChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
  };

  customerValueChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{ label: 'Order Value', data: [] }],
  };
  customerValueChartType: 'bar' = 'bar';
  customerValueChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: { ticks: { maxRotation: 0, minRotation: 0 } },
    },
  };

  lowStockChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{ label: 'Stock Quantity', data: [] }],
  };
  lowStockChartType: 'bar' = 'bar';
  lowStockChartOptions: ChartOptions<'bar'> = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
  };

  monthlyTrendChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [],
  };
  monthlyTrendChartType: 'line' = 'line';
  monthlyTrendChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        type: 'linear',
        position: 'left',
      },
      y1: {
        type: 'linear',
        position: 'right',
        grid: {
          drawOnChartArea: false,
        },
      },
    },
  };
}

