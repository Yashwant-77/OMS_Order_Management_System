import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditOrder } from './edit-order';

describe('EditOrder', () => {
  let component: EditOrder;
  let fixture: ComponentFixture<EditOrder>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditOrder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditOrder);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
