import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BomCreate } from './bom-create';

describe('BomCreate', () => {
  let component: BomCreate;
  let fixture: ComponentFixture<BomCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BomCreate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BomCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
