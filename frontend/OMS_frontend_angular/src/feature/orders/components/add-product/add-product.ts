import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Component} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';



@Component({
  selector: 'app-add-product',
  imports: [CommonModule , FormsModule , MatFormFieldModule , MatIconModule , MatInputModule ],
  templateUrl: './add-product.html',
  styleUrl: './add-product.css',
})
export class AddProduct {


  product = {
    productName: '',
    description: '',
    unitPrice: null,
    quantityInStock: null
  };

  onSubmit() {
    console.log('Product Data:', this.product);

    // TODO: call API
    // this.productService.addProduct(this.product).subscribe(...)
  }

}
