import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AddCustomerDialog } from './add-customer-dialog';

describe('AddCustomerDialog', () => {
  let component: AddCustomerDialog;
  let fixture: ComponentFixture<AddCustomerDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCustomerDialog],
      providers: [
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddCustomerDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
