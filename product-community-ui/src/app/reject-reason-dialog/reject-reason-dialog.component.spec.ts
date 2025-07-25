import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RejectReasonDialogComponent } from './reject-reason-dialog.component';

describe('RejectReasonDialogComponent', () => {
  let component: RejectReasonDialogComponent;
  let fixture: ComponentFixture<RejectReasonDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RejectReasonDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RejectReasonDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
