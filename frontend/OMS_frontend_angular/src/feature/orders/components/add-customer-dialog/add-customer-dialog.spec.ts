import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCustomerDialog } from './add-customer-dialog';

describe('AddCustomerDialog', () => {
  let component: AddCustomerDialog;
  let fixture: ComponentFixture<AddCustomerDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCustomerDialog]
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
