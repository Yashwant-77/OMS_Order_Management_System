import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AddCustomerDialog } from '../add-customer-dialog/add-customer-dialog';
import { ApiOrders } from '../../services/orders-api.service';
import { UserService } from '../../../../app/services/user/user.service';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-create-order',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule, // ✅ IMPORTANT
    MatSnackBarModule,
    RouterModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './create-order.html',
  styleUrl: './create-order.css',
})
export class CreateOrder implements OnInit {
  constructor(
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private userService: UserService,
    private router: Router,
    private orderApiService: ApiOrders,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {}

  @ViewChild('orderForm') form!: NgForm;

  customers: any[] = [];
  selectedCustomerId: number | null = null;
  products: any[] = [];
  order = {
    customerId: null,
    items: [
      {
        productId: null,
        quantity: 1,
        unitPrice: 0,
      },
    ],
    totalAmount: 0,
  };

  isEditMode = false;
  orderId!: number;
  isLoading = false;

  // ================ PRODUCTS and CUSTOMERS FROM API ====================

  ngOnInit() {
    this.getProducts();
    this.getCustomers();
    this.route.params.subscribe((params) => {
      this.orderId = params['id'];

      if (this.orderId) {
        this.isEditMode = true;
        this.loadOrder();
      }
    });
  }

  loadOrder() {
    this.isLoading = true;

    this.orderApiService.getOrderById(this.orderId).subscribe({
      next: (res: any) => {
        this.order = {
          customerId: res.customerId,
          totalAmount: res.totalAmount,
          items: res.items || [],
        };
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error fetching order', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  getProducts() {
    this.orderApiService.getAllProducts().subscribe({
      next: (res: any) => {
        this.products = res;
        console.log('Products:', this.products);
        
      },
      error: (err) => {
        console.error('Error fetching products', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  getCustomers() {
    this.orderApiService.getAllCustomers().subscribe({
      next: (res: any) => {
        this.customers = res;
        console.log('Customers : ', this.customers);
      },
      error: (err) => {
        console.error('Error fetching customers', err);
        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  // ================= DIALOG =================
  openCustomerDialog() {
    const dialogRef = this.dialog.open(AddCustomerDialog, {
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // api call to add new customer in backend

        this.orderApiService.addNewCustomer(result).subscribe({
          next: (res: any) => {
            // ✅ add to list after backend success
            this.customers.push(res);

            // ✅ auto select new customer
            this.order.customerId = res.customerId || null;

            console.log('Customer added:', res);
          },
          error: (err) => {
            console.error('Error adding customer', err);
          },
        });
      }
    });
  }

  // ================= ORDER FUNCTIONS =================

  addItem() {
    this.order.items.push({
      productId: null,
      quantity: 1,
      unitPrice: 0,
    });
  }

  removeItem(index: number) {
    this.order.items.splice(index, 1);
    this.calculateTotal();
  }

  onProductSelect(index: number) {
    const product = this.products.find((p) => p.productId === this.order.items[index].productId);

    if (product) {
      this.order.items[index].unitPrice = product.unitPrice;
    }

    this.calculateTotal();
  }

  calculateTotal() {
    this.order.totalAmount = this.order.items.reduce((sum, item) => {
      return sum + item.quantity * item.unitPrice;
    }, 0);
  }

  // ================= SUBMIT =================
  onSubmit() {
    const payload = {
      customerId: this.order.customerId,
      totalAmount: this.order.totalAmount,
      items: this.order.items,
    };

    console.log('Order Payload:', payload);

    this.isLoading = true;

    const request = this.isEditMode
      ? this.orderApiService.updateOrder(this.orderId, this.order)
      : this.orderApiService.createOrder(payload);

    request.subscribe({
      next: (res) => {
        this.isLoading = false;

        const message = this.isEditMode
          ? '✅ Order updated successfully!'
          : '✅ Order created successfully!';

        console.log(message);

        this.snackBar.open(message, 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });

        if (this.isEditMode) {
          // 👉 navigate after update
          this.router.navigate(['/sales-orders']);
        } else {
          // 👉 reset only for create
          this.form.resetForm();

          this.order = {
            customerId: null,
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

        this.cdr.detectChanges();
      },

      error: (err) => {
        this.isLoading = false;

        console.error('Error:', err);

        this.snackBar.open('❌ Something went wrong', 'Close', {
          duration: 3000,
        });

        if (err.status === 401) {
          this.userService.clear();
          this.router.navigate(['/login']);
        }
      },
    });
  }
}
