
import { CommonModule } from '@angular/common';
import { Component, OnInit, HostListener } from '@angular/core'
import { RouterOutlet } from '@angular/router';
import { UserService } from '../../services/user/user.service';

@Component({
  selector: 'app-home-layout',
  imports: [CommonModule , RouterOutlet],
  templateUrl: './home-layout.html',
  styleUrl: './home-layout.css',
})
export class HomeLayout implements OnInit{

  constructor(private userService : UserService){

  }

  isCollapsed = false;
  mobileOpen = false;
  isMobile = false;

  ngOnInit() {
    this.checkScreen();
  }

  @HostListener('window:resize')
  checkScreen() {
    this.isMobile = window.innerWidth < 768;
    if (!this.isMobile) {
      this.mobileOpen = false;
    }
  }

  toggleSidenav() {
    if (this.isMobile) {
      this.mobileOpen = !this.mobileOpen;
    } else {
      this.isCollapsed = !this.isCollapsed;
    }
  }

  closeMobile() {
    this.mobileOpen = false;
  }

  onNavClick() {
    if (this.isMobile) {
      this.mobileOpen = false;
    }
  }
}
