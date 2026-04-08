import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import {MatPaginatorModule} from '@angular/material/paginator';

@Component({
  selector: 'app-orders',
  imports: [MatPaginatorModule , CommonModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders {

  totalItems = 100;
pageSize = 10;
currentPage = 1;


//  api call here
allData = [] // full data
pagedData: any[] = [];

loadData() {
  const start = (this.currentPage - 1) * this.pageSize;
  const end = start + this.pageSize;
  this.pagedData = this.allData.slice(start, end);
}

get totalPages(): number {
  return Math.ceil(this.totalItems / this.pageSize);
}

get pages(): number[] {
  return Array.from({ length: this.totalPages }, (_, i) => i + 1);
}

get startItem(): number {
  return (this.currentPage - 1) * this.pageSize + 1;
}

get endItem(): number {
  return Math.min(this.currentPage * this.pageSize, this.totalItems);
}

// Navigation
goToPage(page: number) {
  this.currentPage = page;
  this.loadData();
}

nextPage() {
  if (this.currentPage < this.totalPages) {
    this.currentPage++;
    this.loadData();
  }
}

prevPage() {
  if (this.currentPage > 1) {
    this.currentPage--;
    this.loadData();
  }
}

}
