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
    private userService: UserService,
  ) {}

  public callGetAllOrders() {
    const token = this.userService.getToken();

    const requestHeader = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
    return this.http.get(`${environment.baseUrl}/api/orders` , {headers : requestHeader});
  }


  public callDeleteOrder(orderId:any) {
    const token = this.userService.getToken();

    const requestHeader = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
    return this.http.delete(`${environment.baseUrl}/api/orders/${orderId}` , {headers : requestHeader});
  }

}
