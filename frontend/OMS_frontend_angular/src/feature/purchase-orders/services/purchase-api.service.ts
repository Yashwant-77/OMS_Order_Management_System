import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PurchaseApiService {
  constructor(private http: HttpClient) {}

  getAllPurchaseOrders() {
    return this.http.get(`${environment.baseUrl}/api/purchase-orders`);
  }

  getPurchaseOrderById(id: number) {
    return this.http.get(`${environment.baseUrl}/api/purchase-orders/${id}`);
  }

  createPurchaseOrder(payload: any) {
    return this.http.post(`${environment.baseUrl}/api/purchase-orders`, payload);
  }

  updatePurchaseOrderStatus(id: number, status: string) {
    const params = new HttpParams().set('status', status);
    return this.http.put(`${environment.baseUrl}/api/purchase-orders/${id}/status`, null, { params });
  }

  cancelPurchaseOrder(id: number) {
    return this.http.delete(`${environment.baseUrl}/api/purchase-orders/${id}`, { responseType: 'text' });
  }

  getAllSuppliers() {
    return this.http.get(`${environment.baseUrl}/api/suppliers`);
  }

  getAllProducts() {
    return this.http.get(`${environment.baseUrl}/api/products`);
  }

  getLowStockProducts(threshold: number = 10) {
    const params = new HttpParams().set('threshold', threshold);
    return this.http.get(`${environment.baseUrl}/api/reports/products/low-stock`, { params });
  }
}
