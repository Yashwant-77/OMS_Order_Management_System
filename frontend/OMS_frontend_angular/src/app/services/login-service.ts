import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private baseUrl = "http://localhost:8080";


  constructor(private http:HttpClient){

  }

  doLogin(data:any){
    return this.http.post(`${this.baseUrl}/users/login` , data);
  }
  
}
