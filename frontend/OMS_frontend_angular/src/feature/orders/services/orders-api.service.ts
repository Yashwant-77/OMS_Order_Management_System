import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { UserService } from '../../../app/services/user/user.service';

@Injectable({
  providedIn: 'root',
})
export class ApiOrders {
  constructor(
    private http: HttpClient,
  ) {}

  public getAllOrders() {
    return this.http.get(`${environment.baseUrl}/api/orders`);
  }


  public deleteOrder(orderId:any) {
    return this.http.delete(`${environment.baseUrl}/api/orders/${orderId}` );
  }


  public getAllProducts(){
    return this.http.get(`${environment.baseUrl}/api/products`);
  }

  public getAllCustomers(){
    return this.http.get(`${environment.baseUrl}/api/customers` );
  }

  public createOrder(payload:any){
    return this.http.post(`${environment.baseUrl}/api/orders` , payload );
  }


}
