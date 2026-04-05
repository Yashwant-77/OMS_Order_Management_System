import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import { FormsModule, NgForm } from '@angular/forms';

import {MatToolbarModule} from '@angular/material/toolbar';
import { JsonPipe } from '@angular/common';
import { AuthApi } from '../../../../core/services/api/auth.api';
import {MatSnackBar} from '@angular/material/snack-bar';



// import { Navbar } from '../navbar/navbar';

@Component({
  selector: 'app-login',
  imports: [MatFormFieldModule , MatIconModule , MatInputModule , MatSelectModule , MatDividerModule  ,FormsModule , MatButtonModule   , MatToolbarModule , JsonPipe ],
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

  constructor(private loginService:AuthApi , private snak : MatSnackBar){}

  doSubmitForm(){
    console.log("Trying to submit form");

    if(
      // this.credential.username == '' || 
      this.credential.email === '' || this.credential.password == '' || this.credential.role == ''){
      this.snak.open("Fields can not be empty !");
      return;
    }


    this.loginService.callLogin(this.credential).subscribe(
      (response)=> {
        console.log(response)
      } ,
      (error) => {
        console.log(error)
      }
    );

    

  }
}
