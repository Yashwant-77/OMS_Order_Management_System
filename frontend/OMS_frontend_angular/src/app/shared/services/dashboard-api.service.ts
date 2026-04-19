import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DashboardApiService {

  constructor(private http : HttpClient){}


  getPieChartData(){
    return this.http.get(`${environment.baseUrl}/api/reports/orders/summary`);
  }


  getBarGraphData(){
    return this.http.get(`${environment.baseUrl}/api/dashboard/summary`);
  }

  
}
