import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-add-customer-dialog',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './add-customer-dialog.html',
  styleUrl: './add-customer-dialog.css',
})
export class AddCustomerDialog {
  customer = {
    customerName: '',
    email: '',
    phone: '',
    address: ''
  };

  constructor(private dialogRef: MatDialogRef<AddCustomerDialog>) {}

  save() {
    // You can call API here
    this.dialogRef.close(this.customer); // return data
  }

  close() {
    this.dialogRef.close();
  }

}
