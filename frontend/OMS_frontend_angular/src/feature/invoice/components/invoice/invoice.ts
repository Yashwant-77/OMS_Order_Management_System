import { CommonModule } from '@angular/common';
import { Component, OnInit , ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UserService } from '../../../../app/services/user/user.service';
import { ApiOrders } from '../../../orders/services/orders-api.service';
import { InvoiceApiService } from '../../services/invoice-api.service';

interface SalesOrder {
  salesOrderId: number;
  customerName: string;
  orderDate: string;
  totalAmount: number;
  status: string;
}

interface InvoiceRow {
  invoiceId: number;
  salesOrderId: number;
  customerName: string;
  invoiceDate: string;
  totalAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  status: string;
}

interface PaymentRow {
  paymentId: number;
  invoiceId: number;
  paymentDate: string;
  amount: number;
  paymentMethod: string;
  notes?: string;
}

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule, FormsModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './invoice.html',
  styleUrl: './invoice.css',
})
export class Invoice implements OnInit {
  constructor(
    private invoiceApi: InvoiceApiService,
    private ordersApi: ApiOrders,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private router: Router,
    private cdr : ChangeDetectorRef
  ) {}

  isLoading = false;
  isGeneratingInvoice = false;
  selectedOrderId: number | null = null;
  invoices: InvoiceRow[] = [];
  eligibleOrders: SalesOrder[] = [];
  paymentMethods = ['CASH', 'BANK_TRANSFER', 'CHEQUE', 'CREDIT_CARD', 'UPI'];
  paymentState: Record<number, { amount: number | null; paymentMethod: string; notes: string }> = {};
  paymentsMap: Record<number, PaymentRow[]> = {};
  expandedInvoiceId: number | null = null;

  ngOnInit(): void {
    this.loadInvoiceFlow();
  }

  loadInvoiceFlow() {
    this.isLoading = true;

    forkJoin({
      orders: this.ordersApi.getAllOrders(),
      invoices: this.invoiceApi.getAllInvoices(),
    }).subscribe({
      next: ({ orders, invoices }) => {
        const allOrders = (orders || []) as SalesOrder[];
        this.invoices = ((invoices || []) as InvoiceRow[]).sort((a, b) => b.invoiceId - a.invoiceId);

        const invoicedOrderIds = new Set(this.invoices.map((invoice) => invoice.salesOrderId));
        this.eligibleOrders = allOrders.filter((order) => {
          return !invoicedOrderIds.has(order.salesOrderId) && !['PENDING', 'CANCELLED'].includes(order.status);
        });

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.handleError(err, 'Unable to load invoice flow data');
        this.cdr.detectChanges();
      },
    });
  }

  generateInvoice() {
    if (!this.selectedOrderId) {
      return;
    }

    this.isGeneratingInvoice = true;
    this.invoiceApi.generateInvoice(this.selectedOrderId).subscribe({
      next: () => {
        this.snackBar.open('Invoice generated successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.selectedOrderId = null;
        this.isGeneratingInvoice = false;
        this.loadInvoiceFlow();
      },
      error: (err) => {
        this.isGeneratingInvoice = false;
        this.handleError(err, 'Unable to generate invoice');
        this.cdr.detectChanges();
      },
    });
  }

  toggleInvoicePayments(invoiceId: number) {
    if (this.expandedInvoiceId === invoiceId) {
      this.expandedInvoiceId = null;
      return;
    }

    this.expandedInvoiceId = invoiceId;

    if (!this.paymentsMap[invoiceId]) {
      this.invoiceApi.getPaymentsByInvoice(invoiceId).subscribe({
        next: (payments) => {
          this.paymentsMap[invoiceId] = (payments || []) as PaymentRow[];
        },
        error: (err) => {
          this.handleError(err, 'Unable to fetch payment history');
        },
      });
    }
  }

  getPaymentState(invoiceId: number) {
    if (!this.paymentState[invoiceId]) {
      this.paymentState[invoiceId] = {
        amount: null,
        paymentMethod: 'UPI',
        notes: '',
      };
    }
    return this.paymentState[invoiceId];
  }

  recordPayment(invoice: InvoiceRow) {
    const state = this.getPaymentState(invoice.invoiceId);

    if (!state.amount || state.amount <= 0) {
      this.snackBar.open('Please enter a valid payment amount', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.invoiceApi
      .recordPayment({
        invoiceId: invoice.invoiceId,
        amount: state.amount,
        paymentMethod: state.paymentMethod,
        notes: state.notes?.trim() || undefined,
      })
      .subscribe({
        next: () => {
          this.snackBar.open('Payment recorded successfully!', 'Close', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
          });
          this.paymentState[invoice.invoiceId] = { amount: null, paymentMethod: 'UPI', notes: '' };
          this.paymentsMap[invoice.invoiceId] = [];
          this.loadInvoiceFlow();
          this.toggleInvoicePayments(invoice.invoiceId);
        },
        error: (err) => {
          this.handleError(err, 'Unable to record payment');
        },
      });
  }

  private handleError(err: any, fallbackMessage: string) {
    if (err.status === 401) {
      this.userService.clear();
      this.router.navigate(['/login']);
      return;
    }

    this.snackBar.open(err?.error?.message || fallbackMessage, 'Close', {
      duration: 3000,
    });
  }


  getRole(){
    return this.userService.getRole();
  }
}
