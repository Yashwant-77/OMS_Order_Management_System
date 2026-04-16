import { CommonModule } from '@angular/common';
import { Component, NgModule } from '@angular/core';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiOrders } from '../../services/orders-api.service';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../../../app/services/user/user.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-orders',
  imports: [MatPaginatorModule, CommonModule , FormsModule , RouterModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders {
  orders: any[] = [];

  constructor(
    private apiOrders: ApiOrders,
    private router: Router,
    private userService: UserService,
  ) {}

searchText: string = '';
isDropdownOpen: boolean = false;
selectedStatus: string = '';

statuses = [
  'PENDING',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED'
];

filteredOrdersList: any[] = [];



toggleDropdown() {
  this.isDropdownOpen = !this.isDropdownOpen;
}

applyStatusFilter(status: string) {
  this.selectedStatus = status;
  this.isDropdownOpen = false;
  this.applyFilters();
}

applyFilters() {
  this.filteredOrdersList = this.orders.filter(order => {

    const matchesSearch =
      !this.searchText ||
      order.customerName?.toLowerCase().includes(this.searchText.toLowerCase()) ||
      order.salesOrderId?.toString().includes(this.searchText);

    const matchesStatus =
      !this.selectedStatus || order.status === this.selectedStatus;

    return matchesSearch && matchesStatus;
  });
}

  ngOnInit(): void {
    this.getOrders();
    this.filteredOrdersList = this.orders;
  }

  getOrders() {
    // this.apiOrders.callGetAllOrders().subscribe({
    //   next: (res: any) => {
    //     this.orders = res;
    //     console.log('Orders:', this.orders);
    //   },
    //   error: (err) => {
    //     console.error('Error fetching orders', err);
    //     if (err.status === 401) {
    //       this.userService.clear();
    //       this.router.navigate(['/login']);
    //     }
    //   },
    // });
  }

  deleteOrder(orderId: any) {
    console.log('Delete', orderId);
    this.apiOrders.callDeleteOrder(orderId).subscribe({
      next: (res: any) => {
        
      },
      error: (err) => {
        console.error('Error in deleting order !', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
        
      },
    });
  }


  editOrder(order: any) {
    console.log('Edit', order);
  }

  activeMenu: string | null = null;

  toggleMenu(id: string) {
    this.activeMenu = this.activeMenu === id ? null : id;
  }

  

  

  // pagination
  currentPage = 1;
  itemsPerPage = 10;

  get paginatedOrders() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
  return this.filteredOrdersList.slice(start, start + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.filteredOrdersList.length / this.itemsPerPage);
  }

  get visiblePages() {
    const pages = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }













  clearFilters() {
  this.searchText = '';
  this.selectedStatus = '';
  this.filteredOrdersList = [...this.orders];
  this.currentPage = 1;
}
}
