import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewRequestComponent } from './review-request.component';

describe('ReviewRequestComponent', () => {
  let component: ReviewRequestComponent;
  let fixture: ComponentFixture<ReviewRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewRequestComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
