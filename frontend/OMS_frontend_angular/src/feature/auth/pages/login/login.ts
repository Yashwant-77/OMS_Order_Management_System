import {ChangeDetectionStrategy, Component} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import { FormsModule } from '@angular/forms';

import {MatToolbarModule} from '@angular/material/toolbar';
import { JsonPipe } from '@angular/common';
import { AuthApi } from '../../../../core/api/auth.api';
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
  data = {
    username: '',
    email:  '',
    password: '',
    role:''
  }

  constructor(private login:AuthApi , private snak : MatSnackBar){}

  doSubmitForm(){
    console.log("Trying to submit form");

    if(this.data.username == '' || this.data.email === '' || this.data.password == '' || this.data.role == ''){
      this.snak.open("Fields can not be empty !");
      return;
    }

    this.login.login(this.data).subscribe({
      next : ()=> {} , 
      error : () => {} , 
      complete : ()=> {}
    })
  }


}
