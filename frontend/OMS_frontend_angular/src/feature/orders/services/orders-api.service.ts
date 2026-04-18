import { HttpClient, HttpHeaders , HttpParams } from '@angular/common/http';
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

  public getFilteredOrders(page : number , size:number , status? : string , search? : string ) {
     let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status) {
      params = params.set('status', status);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<any>(`${environment.baseUrl}/api/orders/filteredOrders`, { params });
  }


  public deleteOrder(orderId:number) {
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

  public addNewCustomer(customer:any){
    return this.http.post(`${environment.baseUrl}/api/customers` , customer);
  }


}
