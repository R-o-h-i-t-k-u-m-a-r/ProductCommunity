import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PendingReviewRequestsComponent } from './pending-review-requests.component';

describe('PendingReviewRequestsComponent', () => {
  let component: PendingReviewRequestsComponent;
  let fixture: ComponentFixture<PendingReviewRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PendingReviewRequestsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PendingReviewRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
