import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class UserService {

  constructor(){}

  public setRole(role:string){
    localStorage.setItem("role" , role);
  }

  public getRole() : string{
    return JSON.parse(localStorage.getItem("role") || "" );
  }

  public setToken(jwtToken:string){
    localStorage.setItem("jwtToken" , jwtToken);
  }

  public getToken(): string{
    return localStorage.getItem("jwtToken") || "";
  }

  public clear(){
    localStorage.clear();
  }

  public isLoggedIn(){
    return this.getRole() && this.getToken();
  }


  public roleMatch(allowedRoles : String){
    let isMatch = false;

    for(let i = 0 ; i < allowedRoles.length ; ++i){
      if(this.getRole() === allowedRoles[i]){
        isMatch = true;
        return isMatch;
      }
    }
    return isMatch;

  }


  
}
