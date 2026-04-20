import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UserService } from '../../../../app/services/user/user.service';
import { environment } from '../../../../environments/environment';
import { ApiOrders } from '../../../orders/services/orders-api.service';

interface Product {
  productId: number;
  productName: string;
  description?: string;
  unitPrice?: number;
  quantityInStock?: number;
}

interface BomItem {
  componentId: number | null;
  quantity: number;
}

interface BomResponse {
  productBomId: number;
  productId: number;
  productName: string;
  componentId: number;
  componentName: string;
  quantity: number;
}

@Component({
  selector: 'app-bom-create',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    RouterModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './bom-create.html',
  styleUrl: './bom-create.css',
})
export class BomCreate implements OnInit {
  constructor(
    private http: HttpClient,
    private orderApiService: ApiOrders,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  @ViewChild('bomForm') form!: NgForm;

  products: Product[] = [];
  existingComponents: BomResponse[] = [];
  isLoading = false;
  bom = {
    productId: null as number | null,
    components: [
      {
        componentId: null,
        quantity: 1,
      } as BomItem,
    ],
  };

  ngOnInit(): void {
    this.getProducts();
  }

  getProducts() {
    this.isLoading = true;

    this.orderApiService.getAllProducts().subscribe({
      next: (res: any) => {
        console.log("Products : " , res);
        this.products = res || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error fetching products', err);
        this.handleAuthError(err);
      },
    });
  }

  onProductChange() {
    this.bom.components = this.bom.components.map((item) => ({
      componentId: item.componentId === this.bom.productId ? null : item.componentId,
      quantity: item.quantity || 1,
    }));

    this.loadExistingBom();
  }

  loadExistingBom() {
    if (!this.bom.productId) {
      this.existingComponents = [];
      return;
    }

    this.http.get<BomResponse[]>(`${environment.baseUrl}/api/bom/${this.bom.productId}`).subscribe({
      next: (res) => {
        this.existingComponents = res || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.existingComponents = [];
        console.error('Error fetching BOM', err);
        this.handleAuthError(err);
      },
    });
  }

  addItem() {
    this.bom.components.push({
      componentId: null,
      quantity: 1,
    });
  }

  removeItem(index: number) {
    this.bom.components.splice(index, 1);

    if (!this.bom.components.length) {
      this.addItem();
    }
  }

  isComponentAlreadySelected(productId: number, currentIndex: number): boolean {
    return this.bom.components.some((item, index) => {
      return index !== currentIndex && item.componentId === productId;
    });
  }

  onSubmit() {
    if (!this.bom.productId || !this.bom.components.length) {
      return;
    }

    const payloads = this.bom.components
      .filter((item) => item.componentId && item.quantity > 0)
      .map((item) => ({
        productId: this.bom.productId,
        componentId: item.componentId,
        quantity: item.quantity,
      }));

    if (!payloads.length) {
      this.snackBar.open('Please add at least one component', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.isLoading = true;

    const requests = payloads.map((payload) => {
      return this.http.post(`${environment.baseUrl}/api/bom`, payload);
    });

    forkJoin(requests).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('BOM created successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.resetForm();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error creating BOM', err);
        this.snackBar.open(err?.error?.message || 'Something went wrong', 'Close', {
          duration: 3000,
        });
        this.handleAuthError(err);
      },
    });
  }

  resetForm() {
    this.form?.resetForm();
    this.existingComponents = [];
    this.bom = {
      productId: null,
      components: [
        {
          componentId: null,
          quantity: 1,
        },
      ],
    };
  }

  private handleAuthError(err: any) {
    if (err.status === 401) {
      this.userService.clear();
      this.router.navigate(['/login']);
    }
  }
}
