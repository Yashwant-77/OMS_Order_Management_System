import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { environment } from '../../../../environments/environment';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-set-password',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatSnackBarModule, RouterModule,
    MatFormFieldModule, MatIconModule, MatInputModule,
    MatButtonModule, MatToolbarModule, MatProgressSpinnerModule
  ],
  templateUrl: './set-password.html',
  styleUrl: './set-password.css'
})
export class SetPassword implements OnInit {
  token: string | null = null;
  password = '';
  confirmPassword = '';
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.snackBar.open('Invalid or missing token.', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit() {
    if (!this.token) {
      this.snackBar.open('No token found. Please use the link from your email.', 'Close', { duration: 3000 });
      return;
    }
    if (this.password.length < 6) {
      this.snackBar.open('Password must be at least 6 characters long.', 'Close', { duration: 3000 });
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.snackBar.open('Passwords do not match.', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    this.http.post(`${environment.baseUrl}/api/auth/set-password`, {
      token: this.token,
      password: this.password
    }, { responseType: 'text' }).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Password set successfully. You can now log in.', 'Close', { duration: 5000 });
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err?.error?.message || err?.error || 'Failed to set password';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }
}
