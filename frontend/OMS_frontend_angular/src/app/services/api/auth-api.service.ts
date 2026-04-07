import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  

  requestHeader  = new HttpHeaders({
    "No-Auth" : "True"
  })


  constructor(private http:HttpClient){

  }

  callLogin(loginData:any){
    return this.http.post(`${environment.baseUrl}/api/auth/login` , loginData , {headers : this.requestHeader} );
  }

}
