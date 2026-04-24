import { Component,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  step: 1 | 2 = 1;
  isLoading = false;

  // Step 1 data
  email = '';
  role = 'SALES_REPRESENTATIVE';
  roles = ['ADMINISTRATOR', 'SALES_REPRESENTATIVE', 'INVENTORY_MANAGER', 'PURCHASING_MANAGER'];

  // Step 2 data
  otp = '';
  newPassword = '';
  confirmPassword = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  requestOtp() {
    if (!this.email || !this.role) {
      this.showMessage('Please enter email and select a role', 'error');
      return;
    }

    this.isLoading = true;
    this.http.post(`${environment.baseUrl}/api/auth/forgot-password`, {
      email: this.email,
      role: this.role
    }, { responseType: 'text' }).subscribe({
      next: () => {
        this.isLoading = false;
        this.showMessage('OTP has been sent to your email (Check console for dummy email)', 'success');
        this.step = 2; // Move to OTP verification step
        this.cdr.detectChanges(); // Force UI update
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.message || err.error || 'Failed to request OTP. Check credentials.';
        this.showMessage(msg, 'error');
      }
    });
  }

  resetPassword() {
    if (!this.otp || !this.newPassword || !this.confirmPassword) {
      this.showMessage('Please fill all fields', 'error');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.showMessage('Passwords do not match', 'error');
      return;
    }

    if (this.newPassword.length < 6) {
      this.showMessage('Password must be at least 6 characters', 'error');
      return;
    }

    this.isLoading = true;
    this.http.post(`${environment.baseUrl}/api/auth/reset-password-otp`, {
      email: this.email,
      otp: this.otp,
      newPassword: this.newPassword
    }, { responseType: 'text' }).subscribe({
      next: () => {
        this.isLoading = false;
        this.showMessage('Password reset successfully! You can now login.', 'success');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.message || err.error || 'Failed to reset password. Invalid OTP?';
        this.showMessage(msg, 'error');
      }
    });
  }

  private showMessage(message: string, type: 'success' | 'error') {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: type === 'error' ? ['bg-red-500', 'text-white'] : ['bg-green-500', 'text-white'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }
}
