import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Login } from './login';
import { AuthApiService } from '../../../../app/services/api/auth-api.service';
import { UserService } from '../../../../app/services/user/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  
  let mockAuthApiService: any;
  let mockUserService: any;
  let router: Router;

  beforeEach(async () => {
    mockAuthApiService = { callLogin: vi.fn() };
    mockUserService = { setRole: vi.fn(), setToken: vi.fn(), setName: vi.fn(), setEmail: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Login, RouterTestingModule.withRoutes([])],
      providers: [
        { provide: AuthApiService, useValue: mockAuthApiService },
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    vi.spyOn(component['snak'] as any, 'open').mockReturnValue(null as any);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show error snackbar if fields are empty', () => {
    component.credential = { email: '', password: '', role: '' };
    component.doSubmitForm();
    
    expect(component['snak'].open).toHaveBeenCalledWith('Fields can not be empty !');
    expect(mockAuthApiService.callLogin).not.toHaveBeenCalled();
  });

  it('should call login API and navigate to dashboard on success', () => {
    component.credential = { email: 'admin@oms.com', password: 'password', role: 'ADMINISTRATOR' };
    
    const mockResponse = {
      token: 'jwt-123',
      role: 'ADMINISTRATOR',
      name: 'Admin User',
      email: 'admin@oms.com'
    };
    
    mockAuthApiService.callLogin.mockReturnValue(of(mockResponse));
    
    component.doSubmitForm();
    
    expect(component.isLoading).toBe(false);
    expect(mockUserService.setRole).toHaveBeenCalledWith('ADMINISTRATOR');
    expect(mockUserService.setToken).toHaveBeenCalledWith('jwt-123');
    expect(mockUserService.setName).toHaveBeenCalledWith('Admin User');
    expect(mockUserService.setEmail).toHaveBeenCalledWith('admin@oms.com');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should handle login error gracefully', () => {
    component.credential = { email: 'admin@oms.com', password: 'wrong', role: 'ADMINISTRATOR' };
    
    mockAuthApiService.callLogin.mockReturnValue(throwError(() => new Error('Auth failed')));
    
    component.doSubmitForm();
    
    expect(component.isLoading).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
