import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class InvoiceApiService {
  constructor(private http: HttpClient) {}

  getAllInvoices() {
    return this.http.get(`${environment.baseUrl}/api/invoices`);
  }

  getInvoiceById(invoiceId: number) {
    return this.http.get(`${environment.baseUrl}/api/invoices/${invoiceId}`);
  }

  generateInvoice(salesOrderId: number) {
    return this.http.post(`${environment.baseUrl}/api/invoices`, { salesOrderId });
  }

  updateInvoiceStatus(invoiceId: number, status: string) {
    return this.http.put(`${environment.baseUrl}/api/invoices/${invoiceId}/status`, null, {
      params: { status },
    });
  }

  getPaymentsByInvoice(invoiceId: number) {
    return this.http.get(`${environment.baseUrl}/api/payments/invoice/${invoiceId}`);
  }

  recordPayment(payload: { invoiceId: number; amount: number; paymentMethod: string; notes?: string }) {
    return this.http.post(`${environment.baseUrl}/api/payments`, payload);
  }
}
