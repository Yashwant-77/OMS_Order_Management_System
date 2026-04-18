import { Component } from '@angular/core';
import { UserService } from '../../../services/user/user.service';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  user: any;

  constructor(private userService : UserService) {}

  ngOnInit(): void {
    this.user = this.userService.getRole(); // from JWT / localStorage
  }

  treatRoleName(role: string): string {
    return role
      ?.toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

}
