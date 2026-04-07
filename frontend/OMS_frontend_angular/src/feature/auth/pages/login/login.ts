import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import { FormsModule, NgForm } from '@angular/forms';

import {MatToolbarModule} from '@angular/material/toolbar';
import { AuthApiService } from '../../../../app/services/api/auth-api.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import { COLOR_CONSTANTS } from '../../../../app/shared/utils/colorConstants';
import { UserService } from '../../../../app/services/user/user.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-login',
  imports: [MatFormFieldModule , MatIconModule , MatInputModule , MatSelectModule , MatDividerModule  ,FormsModule , MatButtonModule   , MatToolbarModule  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  credential = {
    // username: '',
    email:  '',
    password: '',
    role:''   // DEFAULT ROLE
  }

  colors = COLOR_CONSTANTS;
  constructor(private authApiService:AuthApiService , private snak : MatSnackBar , private userService : UserService , 
    private router : Router
  ){}

  doSubmitForm(){
    console.log("Trying to submit form");

    if(
      // this.credential.username == '' || 
      this.credential.email === '' || this.credential.password == '' || this.credential.role == ''){
      this.snak.open("Fields can not be empty !");
      return;
    }


    this.authApiService.callLogin(this.credential).subscribe(
      (response:any)=> {
        console.log(response)

        // temporary according the bakend response
        this.userService.setRoles(response.user.role);
        this.userService.setToken(response.jwtToken);

        const role = response.user.role;
        if(role == "ADMINISTRATOR"){
          this.router.navigate(["/admin"])
        }
        else if(role == "PRODUCT_MANAGER"){
          this.router.navigate(["/product-manager"])
        }
        else if(role == "SALES_REPRESENTATION"){
          this.router.navigate(["/sales-representation"])
        }
        else if(role == "PURCHASING_OFFICER"){
          this.router.navigate(["/purchasing-officer"])
        }
        else if(role == "FINANCE_MANAGER"){
          this.router.navigate(["/finance-manager"])
        }
        else if(role == "BUSINESS_ANALYST"){
          this.router.navigate(["/business-analyst"])
        }
      } ,
      (error) => {
        console.log(error)
      }
    );

    

  }
}
