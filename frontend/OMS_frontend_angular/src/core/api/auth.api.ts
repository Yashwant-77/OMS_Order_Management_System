import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthApi {
  private baseUrl = "http://localhost:8080";


  constructor(private http:HttpClient){

  }

  callLogin(data:any){
    return this.http.post(`${this.baseUrl}/api/auth/login` , data)
  }

}
