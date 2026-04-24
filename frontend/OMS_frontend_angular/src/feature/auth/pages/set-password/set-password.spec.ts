import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SetPassword } from './set-password';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { vi } from 'vitest';

describe('SetPassword Component', () => {
  let component: SetPassword;
  let fixture: ComponentFixture<SetPassword>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SetPassword, HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ token: 'dummy-url-token' }) }
        }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(SetPassword);
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

  it('should create and load token from query params', () => {
    expect(component).toBeTruthy();
    expect(component.token).toBe('dummy-url-token');
  });

  it('should validate form inputs before submit', () => {
    component.password = 'short';
    component.confirmPassword = 'short';
    component.onSubmit();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('Password must be at least 6 characters long.', 'Close', expect.any(Object));

    component.password = 'password123';
    component.confirmPassword = 'different';
    component.onSubmit();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('Passwords do not match.', 'Close', expect.any(Object));
  });

  it('should prevent submission if token is missing', () => {
    component.token = null;
    component.password = 'password123';
    component.confirmPassword = 'password123';
    
    component.onSubmit();
    
    expect(component['snackBar'].open).toHaveBeenCalledWith('No token found. Please use the link from your email.', 'Close', expect.any(Object));
  });

  it('should call set-password API and navigate to login on success', () => {
    component.token = 'dummy-url-token';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    
    component.onSubmit();
    expect(component.isLoading).toBe(true);

    const req = httpMock.expectOne(`${environment.baseUrl}/api/auth/set-password`);
    expect(req.request.method).toBe('POST');
    req.flush('Set Password Success');

    expect(component.isLoading).toBe(false);
    expect(component['snackBar'].open).toHaveBeenCalledWith('Password set successfully. You can now log in.', 'Close', expect.any(Object));
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should handle API errors gracefully', () => {
    component.token = 'dummy-url-token';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    
    component.onSubmit();

    const req = httpMock.expectOne(`${environment.baseUrl}/api/auth/set-password`);
    req.flush('Token expired', { status: 400, statusText: 'Bad Request' });

    expect(component.isLoading).toBe(false);
    expect(component['snackBar'].open).toHaveBeenCalledWith('Token expired', 'Close', expect.any(Object));
  });
});
