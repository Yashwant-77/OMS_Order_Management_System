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
import { Router } from '@angular/router';

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
    MatDialogModule   // ✅ IMPORTANT
  ],
  templateUrl: './create-order.html',
  styleUrl: './create-order.css',
})
export class CreateOrder implements OnInit {

    constructor(private dialog: MatDialog , private userService : UserService , private router : Router , private orderApiService : ApiOrders) {}

  // ================= CUSTOMER =================
  customers: any[] = [];
  
  selectedCustomerId: number | null = null;

    // ================= ORDER =================
  order = {
    customerId: null,
    items: [
      {
        productId: null,
        quantity: 1,
        unitPrice: 0
      }
    ],
    totalAmount: 0
  };



  // ================= TEMPORARY PRODUCTS =================
products: any[] = [

  // 🔌 Industrial Products
  {
    productId: 1,
    productName: 'Industrial UPS 10kVA',
    unitPrice: 150000,
    quantityInStock: 10
  },
  {
    productId: 2,
    productName: 'Industrial UPS 5kVA',
    unitPrice: 90000,
    quantityInStock: 15
  },
  {
    productId: 3,
    productName: 'Power Transformer 25kVA',
    unitPrice: 250000,
    quantityInStock: 5
  },
  {
    productId: 4,
    productName: 'Switchgear Panel 3 Phase',
    unitPrice: 120000,
    quantityInStock: 8
  },
  {
    productId: 5,
    productName: 'Motor Control Center (MCC)',
    unitPrice: 180000,
    quantityInStock: 6
  },

  // 🏠 Residential Products
  {
    productId: 6,
    productName: 'Home Inverter 1.5kVA',
    unitPrice: 12000,
    quantityInStock: 30
  },
  {
    productId: 7,
    productName: 'Portable Generator 2kW',
    unitPrice: 25000,
    quantityInStock: 20
  },
  {
    productId: 8,
    productName: 'Solar Panel 500W',
    unitPrice: 18000,
    quantityInStock: 25
  },
  {
    productId: 9,
    productName: 'Battery Backup System 150Ah',
    unitPrice: 14000,
    quantityInStock: 40
  },

  // 🔧 Components (for BOM)
  {
    productId: 10,
    productName: 'Circuit Breaker 32A',
    unitPrice: 1200,
    quantityInStock: 100
  },
  {
    productId: 11,
    productName: 'Electrical Relay 24V',
    unitPrice: 800,
    quantityInStock: 150
  },
  {
    productId: 12,
    productName: 'Capacitor 440V',
    unitPrice: 600,
    quantityInStock: 200
  },
  {
    productId: 13,
    productName: 'Copper Cable 1m',
    unitPrice: 150,
    quantityInStock: 500
  },
  {
    productId: 14,
    productName: 'Fuse 10A',
    unitPrice: 50,
    quantityInStock: 300
  }

];

// ================ PRODUCTS and CUSTOMERS FROM API ====================

ngOnInit() {
  this.getProducts();
  this.getCustomers();
}

getProducts() {
  // this.orderApiService.getAllProducts().subscribe({
  //   next: (res: any) => {
  //     this.products = res;
  //     console.log('Products:', this.products);
  //   },
  //   error: (err) => {
  //     console.error('Error fetching products', err);
  //     if(err.status === 401){
  //       this.userService.clear();
  //       this.router.navigate(['/login'])
  //     }
  //   }
  // });
}

getCustomers() {
  // this.orderApiService.getAllCustomers().subscribe({
  //   next: (res: any) => {
  //     this.customers = res;
  //   },
  //   error: (err) => {
  //     console.error('Error fetching customers', err);
  //      if(err.status === 401){
  //       this.userService.clear();
  //       this.router.navigate(['/login'])
  //     }
  //   }
  // });
}




  // ================= DIALOG =================
  openCustomerDialog() {
    const dialogRef = this.dialog.open(AddCustomerDialog, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.customers.push(result);
        this.order.customerId = result.customerId || null;
      }
    });
  }

  // ================= ORDER FUNCTIONS =================

  addItem() {
    this.order.items.push({
      productId: null,
      quantity: 1,
      unitPrice: 0
    });
  }

  removeItem(index: number) {
    this.order.items.splice(index, 1);
    this.calculateTotal();
  }

  onProductSelect(index: number) {
    const product = this.products.find(
      p => p.productId === this.order.items[index].productId
    );

    if (product) {
      this.order.items[index].unitPrice = product.unitPrice;
    }

    this.calculateTotal();
  }

  calculateTotal() {
    this.order.totalAmount = this.order.items.reduce((sum, item) => {
      return sum + (item.quantity * item.unitPrice);
    }, 0);
  }

  // ================= SUBMIT =================
  onSubmit() {
    const payload = {
      customerId: this.order.customerId,
      totalAmount: this.order.totalAmount,
      items: this.order.items
    };

    console.log('Order Payload:', payload);

    // write api call here
  //   this.orderApiService.createOrder(payload).subscribe({
  //   next: (res: any) => {
  //     console.log("order added successfully")
  //   },
  //   error: (err) => {
  //     console.error('Error creating order', err);
  //      if(err.status === 401){
  //       this.userService.clear();
  //       this.router.navigate(['/login'])
  //     }
  //   }
  // });
  }
}