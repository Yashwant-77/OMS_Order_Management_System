import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UserService } from '../../../../app/services/user/user.service';
import { PurchaseApiService } from '../../services/purchase-api.service';

interface Product {
  productId: number;
  productName: string;
  unitPrice: number;
  quantityInStock: number;
}

interface Supplier {
  supplierId: number;
  name: string;
}

interface PurchaseOrderItem {
  productId: number | null;
  quantity: number;
  unitPrice: number;
  productName?: string;
}

interface PurchaseOrder {
  purchaseOrderId: number;
  supplierName: string;
  totalAmount: number;
  orderDate: string;
  status: string;
  items: { productName: string; quantity: number }[];
}

@Component({
  selector: 'app-purchase',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './purchase.html',
  styleUrl: './purchase.css',
})
export class Purchase implements OnInit {
  constructor(
    private purchaseApi: PurchaseApiService,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  isLoading = false;
  isCreateOpen = false;
  isDropdownOpen = false;

  products: Product[] = [];
  suppliers: Supplier[] = [];
  lowStockProducts: Product[] = [];
  allOrders: PurchaseOrder[] = [];
  filteredOrders: PurchaseOrder[] = [];
  paginatedOrders: PurchaseOrder[] = [];

  statuses = ['PENDING', 'APPROVED', 'SHIPPED', 'RECEIVED', 'CANCELLED'];
  selectedStatus = '';
  searchText = '';
  lowStockThreshold = 10;

  currentPage = 0;
  pageSize = 8;
  totalPages = 0;
  visiblePages: number[] = [];

  purchaseOrder: { supplierId: number | null; totalAmount: number; items: PurchaseOrderItem[] } = {
    supplierId: null,
    totalAmount: 0,
    items: [{ productId: null, quantity: 1, unitPrice: 0 }],
  };

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    forkJoin({
      products: this.purchaseApi.getAllProducts(),
      suppliers: this.purchaseApi.getAllSuppliers(),
      orders: this.purchaseApi.getAllPurchaseOrders(),
    }).subscribe({
      next: ({ products, suppliers, orders }) => {
        this.products = (products as Product[]) || [];
        this.suppliers = (suppliers as Supplier[]) || [];
        this.allOrders = ((orders as PurchaseOrder[]) || []).sort(
          (a, b) => b.purchaseOrderId - a.purchaseOrderId,
        );
        this.applyFilters();
        this.getLowStockProducts();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.handleError(err, 'Unable to load purchase orders');
        this.cdr.detectChanges();
      },
    });
  }

  getLowStockProducts() {
    this.purchaseApi.getLowStockProducts(this.lowStockThreshold).subscribe({
      next: (res: any) => {
        this.lowStockProducts = (res as Product[]) || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.lowStockProducts = [];
      },
    });
  }

  addAlertToPurchaseOrder(product: Product) {
    if (!this.isCreateOpen) {
      this.isCreateOpen = true;
    }
    const existing = this.purchaseOrder.items.find((i) => i.productId === product.productId);
    if (!existing) {
      this.purchaseOrder.items.push({
        productId: product.productId,
        quantity: 1,
        unitPrice: product.unitPrice,
        productName: product.productName,
      });
      this.calculateTotal();
    }
  }

  toggleCreateForm() {
    this.isCreateOpen = !this.isCreateOpen;
    if (!this.isCreateOpen) {
      this.resetForm();
    }
  }

  addItem() {
    this.purchaseOrder.items.push({ productId: null, quantity: 1, unitPrice: 0 });
  }

  removeItem(index: number) {
    this.purchaseOrder.items.splice(index, 1);
    if (!this.purchaseOrder.items.length) {
      this.addItem();
    }
    this.calculateTotal();
  }

  onProductSelect(index: number) {
    const item = this.purchaseOrder.items[index];
    const product = this.products.find((p) => p.productId === item.productId);
    if (product) {
      item.unitPrice = product.unitPrice;
      item.productName = product.productName;
    }
    this.calculateTotal();
  }

  calculateTotal() {
    this.purchaseOrder.totalAmount = this.purchaseOrder.items.reduce(
      (sum, item) => sum + (item.quantity || 0) * (item.unitPrice || 0),
      0,
    );
  }

  createPurchaseOrder() {
    if (!this.purchaseOrder.supplierId || !this.purchaseOrder.items.length) {
      return;
    }

    const payload = {
      supplierId: this.purchaseOrder.supplierId,
      items: this.purchaseOrder.items
        .filter((i) => i.productId && i.quantity > 0)
        .map((i) => ({ productId: i.productId, quantity: i.quantity, unitPrice: i.unitPrice })),
    };

    this.purchaseApi.createPurchaseOrder(payload).subscribe({
      next: () => {
        this.snackBar.open('Purchase order created!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.resetForm();
        this.isCreateOpen = false;
        this.loadData();
      },
      error: (err) => this.handleError(err, 'Failed to create purchase order'),
    });
  }

  updateStatus(order: PurchaseOrder, newStatus: string) {
    this.purchaseApi.updatePurchaseOrderStatus(order.purchaseOrderId, newStatus).subscribe({
      next: (updated: any) => {
        order.status = (updated as PurchaseOrder).status;
        this.cdr.detectChanges();
      },
      error: (err) => this.handleError(err, 'Failed to update status'),
    });
  }

  cancelPurchaseOrder(orderId: number) {
    this.purchaseApi.cancelPurchaseOrder(orderId).subscribe({
      next: () => {
        this.snackBar.open('Order cancelled.', 'Close', { duration: 3000 });
        this.loadData();
      },
      error: (err) => this.handleError(err, 'Failed to cancel purchase order'),
    });
  }

  resetForm() {
    this.purchaseOrder = {
      supplierId: null,
      totalAmount: 0,
      items: [{ productId: null, quantity: 1, unitPrice: 0 }],
    };
  }

  applyFilters() {
    let result = [...this.allOrders];

    if (this.selectedStatus) {
      result = result.filter((o) => o.status === this.selectedStatus);
    }

    if (this.searchText.trim()) {
      const term = this.searchText.toLowerCase();
      result = result.filter((o) => o.supplierName?.toLowerCase().includes(term));
    }

    this.filteredOrders = result;
    this.totalPages = Math.ceil(this.filteredOrders.length / this.pageSize);
    this.currentPage = 0;
    this.buildVisiblePages();
    this.paginate();
  }

  applyStatusFilter(status: string) {
    this.selectedStatus = status;
    this.isDropdownOpen = false;
    this.currentPage = 0;
    this.applyFilters();
  }

  clearFilters() {
    this.searchText = '';
    this.selectedStatus = '';
    this.applyFilters();
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  changePage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.buildVisiblePages();
    this.paginate();
  }

  paginate() {
    const start = this.currentPage * this.pageSize;
    this.paginatedOrders = this.filteredOrders.slice(start, start + this.pageSize);
  }

  buildVisiblePages() {
    const total = this.totalPages;
    const current = this.currentPage;
    const pages: number[] = [];
    const range = 2;
    for (let i = Math.max(0, current - range); i <= Math.min(total - 1, current + range); i++) {
      pages.push(i);
    }
    this.visiblePages = pages;
  }

  getRole() {
    return this.userService.getRole();
  }

  private handleError(err: any, fallback: string) {
    if (err.status === 401) {
      this.userService.clear();
      this.router.navigate(['/login']);
      return;
    }
    this.snackBar.open(err?.error?.message || fallback, 'Close', { duration: 3000 });
  }
}
