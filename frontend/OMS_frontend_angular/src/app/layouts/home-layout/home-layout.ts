import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { environment } from '../../../environments/environment';
import { UserService } from '../../services/user/user.service';

interface NotificationItem {
  notificationId: number;
  title: string;
  message: string;
  readStatus: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-home-layout',
  imports: [CommonModule, RouterOutlet, MatButtonModule, MatMenuModule, MatListModule, RouterModule],
  templateUrl: './home-layout.html',
  styleUrl: './home-layout.css',
})
export class HomeLayout implements OnInit, OnDestroy {
  constructor(
    private userService: UserService,
    private router: Router,
    private http: HttpClient,
  ) {}

  isCollapsed = false;
  mobileOpen = false;
  isMobile = false;
  role = '';
  notifications: NotificationItem[] = [];
  unreadCount = 0;
  private notificationInterval: any;

  ngOnInit() {
    this.checkScreen();
    this.role = this.userService.getRole();
    this.loadNotifications();
    this.notificationInterval = setInterval(() => {
      this.loadNotifications();
    }, 30000);
  }

  ngOnDestroy() {
    if (this.notificationInterval) {
      clearInterval(this.notificationInterval);
    }
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

  logout() {
    this.userService.clear();
    this.router.navigate(['/login']);
  }

  loadNotifications() {
    this.http.get<NotificationItem[]>(`${environment.baseUrl}/api/notifications`).subscribe({
      next: (res) => {
        this.notifications = res || [];
        this.unreadCount = this.notifications.filter((notification) => !notification.readStatus).length;
      },
      error: (err) => {
        console.error('Error fetching notifications', err);
      },
    });
  }

  markAsRead(notification: NotificationItem) {
    if (notification.readStatus) {
      return;
    }

    this.http.put(`${environment.baseUrl}/api/notifications/${notification.notificationId}/read`, {}).subscribe({
      next: () => {
        notification.readStatus = true;
        this.unreadCount = Math.max(this.unreadCount - 1, 0);
      },
      error: (err) => {
        console.error('Error marking notification as read', err);
      },
    });
  }

  markAllNotificationsAsRead() {
    this.http.put(`${environment.baseUrl}/api/notifications/read-all`, {}).subscribe({
      next: () => {
        this.notifications = this.notifications.map((notification) => ({
          ...notification,
          readStatus: true,
        }));
        this.unreadCount = 0;
      },
      error: (err) => {
        console.error('Error marking notifications as read', err);
      },
    });
  }
}
