import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ForgotPassword } from './forgot-password';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { vi } from 'vitest';

describe('ForgotPassword Component', () => {
  let component: ForgotPassword;
  let fixture: ComponentFixture<ForgotPassword>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForgotPassword, HttpClientTestingModule, RouterTestingModule.withRoutes([])]
    }).compileComponents();
    
    fixture = TestBed.createComponent(ForgotPassword);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    vi.spyOn(component['snackBar'] as any, 'open').mockReturnValue(null as any);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not request OTP if fields are empty', () => {
    component.email = '';
    component.role = '';
    component.requestOtp();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith(
      'Please enter email and select a role', 
      'Close', 
      expect.any(Object)
    );
  });

  it('should call forgot-password API and move to step 2 on success', () => {
    component.email = 'test@oms.com';
    component.role = 'ADMINISTRATOR';
    
    component.requestOtp();
    expect(component.isLoading).toBe(true);

    const req = httpMock.expectOne(`${environment.baseUrl}/api/auth/forgot-password`);
    expect(req.request.method).toBe('POST');
    req.flush('OTP Sent');

    expect(component.isLoading).toBe(false);
    expect(component.step).toBe(2);
    expect(component['snackBar'].open).toHaveBeenCalledWith(
      'OTP has been sent to your email (Check console for dummy email)', 
      'Close', 
      expect.any(Object)
    );
  });

  it('should handle OTP request error', () => {
    component.email = 'test@oms.com';
    component.role = 'ADMINISTRATOR';
    
    component.requestOtp();

    const req = httpMock.expectOne(`${environment.baseUrl}/api/auth/forgot-password`);
    req.flush({ message: 'Error occurred' }, { status: 400, statusText: 'Bad Request' });

    expect(component.isLoading).toBe(false);
    expect(component.step).toBe(1); // Should stay on step 1
  });

  it('should validate reset password fields', () => {
    component.step = 2;
    component.otp = '';
    component.resetPassword();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('Please fill all fields', 'Close', expect.any(Object));

    component.otp = '123456';
    component.newPassword = 'password1';
    component.confirmPassword = 'password0'; // Mismatch
    component.resetPassword();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('Passwords do not match', 'Close', expect.any(Object));

    component.newPassword = 'pass';
    component.confirmPassword = 'pass'; // Too short
    component.resetPassword();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('Password must be at least 6 characters', 'Close', expect.any(Object));
  });

  it('should call reset-password API and navigate to login on success', () => {
    component.step = 2;
    component.email = 'test@oms.com';
    component.otp = '123456';
    component.newPassword = 'password123';
    component.confirmPassword = 'password123';
    
    component.resetPassword();
    expect(component.isLoading).toBe(true);

    const req = httpMock.expectOne(`${environment.baseUrl}/api/auth/reset-password-otp`);
    expect(req.request.method).toBe('POST');
    req.flush('Reset Success');

    expect(component.isLoading).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
