import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Profile } from './profile';
import { UserService } from '../../../services/user/user.service';
import { vi } from 'vitest';

describe('Profile', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockUserService: any;

  beforeEach(async () => {
    mockUserService = {
      getRole: vi.fn().mockReturnValue('SALES_REPRESENTATIVE'),
      getName: vi.fn().mockReturnValue('Alice Wonderland'),
      getEmail: vi.fn().mockReturnValue('alice@oms.com')
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user data on init', () => {
    expect(component.role).toBe('SALES_REPRESENTATIVE');
    expect(component.name).toBe('Alice Wonderland');
    expect(component.email).toBe('alice@oms.com');
    expect(mockUserService.getRole).toHaveBeenCalled();
    expect(mockUserService.getName).toHaveBeenCalled();
    expect(mockUserService.getEmail).toHaveBeenCalled();
  });

  it('should correctly generate initials from name', () => {
    expect(component.initials).toBe('AW');
    
    component.name = 'John Doe';
    expect(component.initials).toBe('JD');

    component.name = 'singleword';
    expect(component.initials).toBe('S');
    
    component.name = '';
    expect(component.initials).toBe('U'); // fallback to U
  });

  it('should format role names correctly', () => {
    expect(component.treatRoleName('SALES_REPRESENTATIVE')).toBe('Sales Representative');
    expect(component.treatRoleName('ADMINISTRATOR')).toBe('Administrator');
  });
});
