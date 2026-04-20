import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { UserService } from '../../../../app/services/user/user.service';

interface Product {
  productId: number;
  productName: string;
  unitPrice: number;
  quantityInStock: number;
}

interface Supplier {
  supplierId: number;
  name: string;
  email: string;
  phone: string;
  address: string;
}

interface LowStockProduct {
  productId: number;
  productName: string;
  quantityInStock: number;
  unitPrice: number;
}

interface PurchaseItem {
  productId: number | null;
  quantity: number;
  unitPrice: number;
}

@Component({
  selector: 'app-purchase',
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
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
    private http: HttpClient,
    private userService: UserService,
    private router: Router,
    private snack: MatSnackBar,
    private cdr: ChangeDetectorRef,
  ) {}

  @ViewChild('purchaseForm') form!: NgForm;

  purchaseOrders: any[] = [];
  filteredOrders: any[] = [];
  paginatedOrders: any[] = [];
  suppliers: Supplier[] = [];
  products: Product[] = [];
  lowStockProducts: LowStockProduct[] = [];

  statuses = ['PENDING', 'APPROVED', 'SHIPPED', 'RECEIVED', 'CANCELLED'];
  selectedStatus = '';
  searchText = '';
  isDropdownOpen = false;
  isLoading = false;
  isCreateOpen = false;
  lowStockThreshold = 10;

  currentPage = 0;
  itemsPerPage = 10;
  totalPages = 0;

  purchaseOrder = {
    supplierId: null as number | null,
    items: [
      {
        productId: null,
        quantity: 1,
        unitPrice: 0,
      } as PurchaseItem,
    ],
    totalAmount: 0,
  };

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData() {
    this.isLoading = true;

    this.getPurchaseOrders();
    this.getSuppliers();
    this.getProducts();
    this.getLowStockProducts();
  }

  getPurchaseOrders() {
    this.http.get<any[]>(`${environment.baseUrl}/api/purchase-orders`).subscribe({
      next: (res) => {
        this.purchaseOrders = res || [];
        this.applyFilters();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error fetching purchase orders', err);
        this.handleAuthError(err);
      },
    });
  }

  getSuppliers() {
    this.http.get<Supplier[]>(`${environment.baseUrl}/api/suppliers`).subscribe({
      next: (res) => {
        this.suppliers = res || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching suppliers', err);
        this.handleAuthError(err);
      },
    });
  }

  getProducts() {
    this.http.get<Product[]>(`${environment.baseUrl}/api/products`).subscribe({
      next: (res) => {
        this.products = res || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching products', err);
        this.handleAuthError(err);
      },
    });
  }

  getLowStockProducts() {
    const params = new HttpParams().set('threshold', this.lowStockThreshold);

    this.http.get<LowStockProduct[]>(`${environment.baseUrl}/api/reports/products/low-stock`, { params }).subscribe({
      next: (res) => {
        this.lowStockProducts = res || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching stock alerts', err);
        this.handleAuthError(err);
      },
    });
  }

  toggleCreateForm() {
    this.isCreateOpen = !this.isCreateOpen;
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  applyFilters() {
    const search = this.searchText.trim().toLowerCase();

    this.filteredOrders = this.purchaseOrders.filter((order) => {
      const matchesStatus = !this.selectedStatus || order.status === this.selectedStatus;
      const matchesSearch =
        !search ||
        order.supplierName?.toLowerCase().includes(search) ||
        String(order.purchaseOrderId).includes(search);

      return matchesStatus && matchesSearch;
    });

    this.totalPages = Math.ceil(this.filteredOrders.length / this.itemsPerPage);
    this.currentPage = Math.min(this.currentPage, Math.max(this.totalPages - 1, 0));
    this.setPaginatedOrders();
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
    this.currentPage = 0;
    this.applyFilters();
  }

  setPaginatedOrders() {
    const start = this.currentPage * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedOrders = this.filteredOrders.slice(start, end);
  }

  changePage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.setPaginatedOrders();
    }
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

  addItem() {
    this.purchaseOrder.items.push({
      productId: null,
      quantity: 1,
      unitPrice: 0,
    });
  }

  removeItem(index: number) {
    this.purchaseOrder.items.splice(index, 1);

    if (!this.purchaseOrder.items.length) {
      this.addItem();
    }

    this.calculateTotal();
  }

  onProductSelect(index: number) {
    const product = this.products.find((p) => {
      return p.productId === this.purchaseOrder.items[index].productId;
    });

    if (product) {
      this.purchaseOrder.items[index].unitPrice = product.unitPrice || 0;
    }

    this.calculateTotal();
  }

  addAlertToPurchaseOrder(product: LowStockProduct) {
    this.isCreateOpen = true;

    const existingItem = this.purchaseOrder.items.find((item) => {
      return item.productId === product.productId;
    });

    if (existingItem) {
      existingItem.quantity += this.getSuggestedQuantity(product);
    } else {
      const emptyItem = this.purchaseOrder.items.find((item) => !item.productId);
      const newItem = {
        productId: product.productId,
        quantity: this.getSuggestedQuantity(product),
        unitPrice: product.unitPrice || 0,
      };

      if (emptyItem) {
        Object.assign(emptyItem, newItem);
      } else {
        this.purchaseOrder.items.push(newItem);
      }
    }

    this.calculateTotal();
    this.cdr.detectChanges();
  }

  getSuggestedQuantity(product: LowStockProduct): number {
    return Math.max(this.lowStockThreshold - product.quantityInStock, 1);
  }

  calculateTotal() {
    this.purchaseOrder.totalAmount = this.purchaseOrder.items.reduce((sum, item) => {
      return sum + (item.quantity || 0) * (item.unitPrice || 0);
    }, 0);
  }

  createPurchaseOrder() {
    const payload = {
      supplierId: this.purchaseOrder.supplierId,
      items: this.purchaseOrder.items
        .filter((item) => item.productId && item.quantity > 0)
        .map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
        })),
    };

    if (!payload.supplierId || !payload.items.length) {
      this.snack.open('Please select supplier and at least one product', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.isLoading = true;

    this.http.post(`${environment.baseUrl}/api/purchase-orders`, payload).subscribe({
      next: () => {
        this.snack.open('Purchase order created successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.resetForm();
        this.getPurchaseOrders();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error creating purchase order', err);
        this.snack.open(err?.error?.message || 'Something went wrong', 'Close', {
          duration: 3000,
        });
        this.handleAuthError(err);
      },
    });
  }

  updateStatus(order: any, status: string) {
    const params = new HttpParams().set('status', status);

    this.http.put(`${environment.baseUrl}/api/purchase-orders/${order.purchaseOrderId}/status`, {}, { params }).subscribe({
      next: () => {
        this.snack.open('Purchase order status updated successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.getPurchaseOrders();
        this.getLowStockProducts();
      },
      error: (err) => {
        console.error('Error updating purchase order status', err);
        this.snack.open(err?.error?.message || 'Something went wrong', 'Close', {
          duration: 3000,
        });
        this.handleAuthError(err);
      },
    });
  }

  cancelPurchaseOrder(orderId: number) {
    this.http.delete(`${environment.baseUrl}/api/purchase-orders/${orderId}`, { responseType: 'text' }).subscribe({
      next: () => {
        this.snack.open('Purchase order cancelled successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.getPurchaseOrders();
      },
      error: (err) => {
        console.error('Error cancelling purchase order', err);
        this.snack.open(err?.error?.message || 'Something went wrong', 'Close', {
          duration: 3000,
        });
        this.handleAuthError(err);
      },
    });
  }

  resetForm() {
    this.form?.resetForm();
    this.purchaseOrder = {
      supplierId: null,
      items: [
        {
          productId: null,
          quantity: 1,
          unitPrice: 0,
        },
      ],
      totalAmount: 0,
    };
  }

  getRole(): string {
    return this.userService.getRole();
  }

  private handleAuthError(err: any) {
    if (err.status === 401) {
      this.userService.clear();
      this.router.navigate(['/login']);
    }
  }
}
