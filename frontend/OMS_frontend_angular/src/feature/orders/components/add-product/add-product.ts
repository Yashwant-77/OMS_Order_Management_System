import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Component} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';



@Component({
  selector: 'app-add-product',
  imports: [CommonModule , FormsModule , MatFormFieldModule , MatIconModule , MatInputModule , MatSelectModule ],
  templateUrl: './add-product.html',
  styleUrl: './add-product.css',
})
export class AddProduct {

  customers: any[] = [];
selectedCustomerId: number | null = null;


  product = {
    productName: '',
    description: '',
    unitPrice: null,
    quantityInStock: null
  };

  onSubmit() {
  const order = {
    customerId: this.selectedCustomerId,
    // product + other fields
  };

  console.log(order);
}

showAddCustomer = false;

newCustomer = {
  customerName: '',
  email: '',
  phone: '',
  address: ''
};

saveCustomer(){}

}
