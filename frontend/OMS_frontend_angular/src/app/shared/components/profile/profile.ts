import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../services/user/user.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  role: string = '';
  name: string = '';
  email: string = '';

  constructor(private userService : UserService) {}

  ngOnInit(): void {
    this.role = this.userService.getRole(); // from JWT / localStorage
    this.name = this.userService.getName();
    this.email = this.userService.getEmail();
  }

  get initials(): string {
    if (!this.name) return 'U';
    return this.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  }

  treatRoleName(role: string): string {
    return role
      ?.toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

}
