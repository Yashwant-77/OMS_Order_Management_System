
import { CommonModule } from '@angular/common';
import { Component, OnInit, HostListener } from '@angular/core'
import { Router, RouterOutlet } from '@angular/router';
import { UserService } from '../../services/user/user.service';
import { MatButtonModule } from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatListModule} from '@angular/material/list';

@Component({
  selector: 'app-home-layout',
  imports: [CommonModule , RouterOutlet , MatButtonModule , MatMenuModule , MatListModule],
  templateUrl: './home-layout.html',
  styleUrl: './home-layout.css',
})
export class HomeLayout implements OnInit{

  constructor(private userService : UserService , private router : Router){

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

  logout(){
    this.userService.clear();
    this.router.navigate(['/login'])
  }
}
