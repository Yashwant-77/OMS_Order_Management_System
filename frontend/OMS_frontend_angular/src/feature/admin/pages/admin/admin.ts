import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  imports: [CommonModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin {
  orders = [
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  { id: '#ORD-1041', customer: 'Ravi Sharma', product: 'Office Supplies', amount: 2400, status: 'Delivered' },
  { id: '#ORD-1042', customer: 'Amit Singh', product: 'Laptop', amount: 55000, status: 'Pending' },
  // 👉 add more dummy data (20–30 rows)
];

// pagination
currentPage = 1;
itemsPerPage = 10;

get paginatedOrders() {
  const start = (this.currentPage - 1) * this.itemsPerPage;
  return this.orders.slice(start, start + this.itemsPerPage);
}

get totalPages() {
  return Math.ceil(this.orders.length / this.itemsPerPage);
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

}
