import { Component } from '@angular/core';
import { MatToolbar } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterModule , MatToolbar],
  templateUrl: './forbidden.html',
  styleUrl: './forbidden.css',
})
export class Forbidden {

}
