import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface OrderSummaryReport {
  totalOrders: number;
  pendingOrders: number;
  confirmedOrders: number;
  processingOrders: number;
  shippedOrders: number;
  deliveredOrders: number;
  cancelledOrders: number;
  totalOrderValue: number;
}

export interface RevenueReport {
  totalInvoiced: number;
  totalCollected: number;
  outstanding: number;
  totalInvoices: number;
  paidInvoices: number;
  partiallyPaidInvoices: number;
  pendingInvoices: number;
}

export interface LowStockReport {
  productId: number;
  productName: string;
  quantityInStock: number;
  unitPrice: number;
}

export interface CustomerOrderReport {
  customerId: number;
  customerName: string;
  customerEmail: string;
  totalOrders: number;
  totalOrderValue: number;
}

export interface PaymentSummaryReport {
  paymentMethod: string;
  totalPayments: number;
  totalAmount: number;
}

export interface MonthlyOrderTrendReport {
  month: string;
  monthNumber: number;
  orderCount: number;
  totalOrderValue: number;
}

export interface MonthlyPaymentTrendReport {
  month: string;
  monthNumber: number;
  totalPaymentAmount: number;
}

@Injectable({
  providedIn: 'root',
})
export class ReportsApiService {
  constructor(private http: HttpClient) {}

  getOrderSummary() {
    return this.http.get<OrderSummaryReport>(`${environment.baseUrl}/api/reports/orders/summary`);
  }

  getRevenueReport() {
    return this.http.get<RevenueReport>(`${environment.baseUrl}/api/reports/revenue`);
  }

  getLowStockReport(threshold: number = 10) {
    const params = new HttpParams().set('threshold', threshold);
    return this.http.get<LowStockReport[]>(`${environment.baseUrl}/api/reports/products/low-stock`, { params });
  }

  getCustomerOrderReport() {
    return this.http.get<CustomerOrderReport[]>(`${environment.baseUrl}/api/reports/customers/orders`);
  }

  getPaymentSummary() {
    return this.http.get<PaymentSummaryReport[]>(`${environment.baseUrl}/api/reports/payments/summary`);
  }

  getMonthlyOrderTrend(year: number) {
    const params = new HttpParams().set('year', year);
    return this.http.get<MonthlyOrderTrendReport[]>(`${environment.baseUrl}/api/reports/orders/monthly-trend`, { params });
  }

  getMonthlyPaymentTrend(year: number) {
    const params = new HttpParams().set('year', year);
    return this.http.get<MonthlyPaymentTrendReport[]>(`${environment.baseUrl}/api/reports/payments/monthly-trend`, { params });
  }
}
