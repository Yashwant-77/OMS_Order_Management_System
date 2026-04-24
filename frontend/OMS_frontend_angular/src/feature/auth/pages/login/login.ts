import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule, NgForm } from '@angular/forms';

import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthApiService } from '../../../../app/services/api/auth-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { COLOR_CONSTANTS } from '../../../../app/shared/utils/colorConstants';
import { UserService } from '../../../../app/services/user/user.service';
import { Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [CommonModule, RouterModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatDividerModule, FormsModule, MatButtonModule, MatToolbarModule, MatProgressSpinnerModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  credential = {
    // username: '',
    email: '',
    password: '',
    role: '', // DEFAULT ROLE
  };

  isLoading = false;

  colors = COLOR_CONSTANTS;
  constructor(
    private authApiService: AuthApiService,
    private snak: MatSnackBar,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  doSubmitForm() {
    console.log('Trying to submit form');

    if (
      this.credential.email === '' ||
      this.credential.password == '' ||
      this.credential.role == ''
    ) {
      this.snak.open('Failed to login , try again !', 'Close', {
          duration: 2500,
        });
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    this.authApiService.callLogin(this.credential).subscribe(
      (response: any) => {
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log(response);

        // temporary according the bakend response
        this.userService.setRole(response.role);
        this.userService.setToken(response.token);
        this.router.navigate(['/dashboard']);
      },
      (error) => {
        console.log(error);
        this.isLoading = false;
        this.snak.open('Failed to login , try again !', 'Close', {
          duration: 2500,
        });

        this.cdr.detectChanges();
      },
    );
  }
}
