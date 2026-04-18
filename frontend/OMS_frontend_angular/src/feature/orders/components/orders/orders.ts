import { CommonModule } from '@angular/common';
import { Component, NgModule } from '@angular/core';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiOrders } from '../../services/orders-api.service';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../../../app/services/user/user.service';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-orders',
  imports: [
    MatPaginatorModule,
    CommonModule,
    FormsModule,
    RouterModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders {
  orders: any[] = [];

  constructor(
    private snack: MatSnackBar,
    private apiOrdersService: ApiOrders,
    private router: Router,
    private userService: UserService,
    private cd: ChangeDetectorRef,
  ) {}

  statuses = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  getRole(): string{
    return this.userService.getRole();
  }

  clearFilters() {
    this.searchText = '';
    this.selectedStatus = '';
  }

  deleteOrder(orderId: number) {
    console.log('Delete', orderId);
    this.apiOrdersService.deleteOrder(orderId).subscribe({
      next: (res: any) => {
        console.log('sucessfully deleted the order', res);

        this.orders = this.orders.filter((order) => order.id !== orderId);

        this.snack.open('✅ Order deleted successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });

        this.cd.detectChanges();
      },
      error: (err) => {
        console.log('Status code : ', err.status);
        console.error('Error in deleting order !', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  activeMenu: string | null = null;

  toggleMenu(id: string) {
    this.activeMenu = this.activeMenu === id ? null : id;
  }

  // pagination
  currentPage = 0;
  itemsPerPage = 10;
  totalPages = 0;

  paginatedOrders: any[] = [];

  searchText: string = '';
  isDropdownOpen: boolean = false;
  selectedStatus: string = '';

  ngOnInit(): void {
    this.getOrders();
  }

  getOrders() {
    this.isLoading = true;
    this.apiOrdersService
      .getFilteredOrders(this.currentPage, this.itemsPerPage, this.selectedStatus, this.searchText)
      .subscribe({
        next: (res: any) => {
          console.log(res);

          this.paginatedOrders = [...res.content];
          this.totalPages = res.totalPages;
          this.isLoading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error fetching orders', err);
          if (err.status === 401) {
            this.userService.clear();
            this.router.navigate(['/login']);
            this.isLoading = false;
          }
        },
      });
  }

  applyFilters() {
    this.currentPage = 0;
    this.getOrders();
  }

  applyStatusFilter(status: string) {
    this.selectedStatus = status;
    this.isDropdownOpen = false;
    this.applyFilters();
  }

  get visiblePages() {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  changePage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.getOrders(); // fetch new page
    }
  }

  // ==================== SPINNER LOGI =====================================

  isLoading = false;
}
