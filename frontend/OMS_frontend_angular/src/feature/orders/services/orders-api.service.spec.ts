import { TestBed } from '@angular/core/testing';

import { ApiOrders } from './orders-api.service';

describe('ApiOrders', () => {
  let service: ApiOrders;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApiOrders);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
