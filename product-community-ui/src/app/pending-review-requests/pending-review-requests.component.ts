import { Component } from '@angular/core';
import { ReviewRequestDTO } from '../_models/product.model';
import { ReviewService } from '../_services/review.service';
import { MatDialog } from '@angular/material/dialog';
import { RejectReasonDialogComponent } from '../reject-reason-dialog/reject-reason-dialog.component';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-pending-review-requests',
  imports: [NgIf, NgFor],
  templateUrl: './pending-review-requests.component.html',
  styleUrl: './pending-review-requests.component.css'
})
export class PendingReviewRequestsComponent {
  pendingRequests: ReviewRequestDTO[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private reviewRequestService: ReviewService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadPendingRequests();
  }

  loadPendingRequests(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.reviewRequestService.getPendingReviewRequests().subscribe({
      next: (response) => {
        this.pendingRequests = response.data;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load pending requests. Please try again.';
        this.isLoading = false;
        console.error(error);
      }
    });
  }

  approveRequest(requestId: number): void {
    this.isLoading = true;
    this.reviewRequestService.approveReviewRequest(requestId).subscribe({
      next: () => {
        this.loadPendingRequests();
      },
      error: (error) => {
        this.errorMessage = 'Failed to approve request. Please try again.';
        this.isLoading = false;
        console.error(error);
      }
    });
  }

  openRejectDialog(requestId: number): void {
    const dialogRef = this.dialog.open(RejectReasonDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(reason => {
      if (reason !== undefined) {
        this.rejectRequest(requestId, reason);
      }
    });
  }

  rejectRequest(requestId: number, reason?: string): void {
    this.isLoading = true;
    this.reviewRequestService.rejectReviewRequest(requestId, reason).subscribe({
      next: () => {
        this.loadPendingRequests();
      },
      error: (error) => {
        this.errorMessage = 'Failed to reject request. Please try again.';
        this.isLoading = false;
        console.error(error);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

}
