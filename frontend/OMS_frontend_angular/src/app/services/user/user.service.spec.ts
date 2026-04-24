import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserService);
    
    // Clear localStorage before each test
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set and get role', () => {
    service.setRole('ADMINISTRATOR');
    expect(service.getRole()).toBe('ADMINISTRATOR');
  });

  it('should set and get token', () => {
    service.setToken('dummy-token-123');
    expect(service.getToken()).toBe('dummy-token-123');
  });

  it('should set and get name', () => {
    service.setName('John Doe');
    expect(service.getName()).toBe('John Doe');
  });

  it('should return default name "User" if not set', () => {
    expect(service.getName()).toBe('User');
  });

  it('should set and get email', () => {
    service.setEmail('john@example.com');
    expect(service.getEmail()).toBe('john@example.com');
  });

  it('should determine if user is logged in', () => {
    expect(service.isLoggedIn()).toBeFalsy();
    
    service.setRole('ADMIN');
    expect(service.isLoggedIn()).toBeFalsy(); // missing token
    
    service.setToken('token');
    expect(service.isLoggedIn()).toBeTruthy();
  });

  it('should check if role matches', () => {
    service.setRole('ADMINISTRATOR');
    
    expect(service.roleMatch(['ADMINISTRATOR'])).toBeTruthy();
    expect(service.roleMatch(['SALES_REPRESENTATIVE', 'ADMINISTRATOR'])).toBeTruthy();
    expect(service.roleMatch(['SALES_REPRESENTATIVE'])).toBeFalsy();
  });

  it('should clear local storage', () => {
    service.setRole('ADMIN');
    service.setToken('token');
    
    service.clear();
    
    expect(service.getRole()).toBe('');
    expect(service.getToken()).toBe('');
  });
});
