import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../_services/review.service';
import { ReviewResponse } from '../_models/product.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RejectReasonDialogComponent } from '../reject-reason-dialog/reject-reason-dialog.component';

@Component({
  selector: 'app-pending-reviews',
   imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './pending-reviews.component.html',
  styleUrl: './pending-reviews.component.css'
})
export class PendingReviewsComponent implements OnInit {
  pendingReviews: ReviewResponse[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private reviewService: ReviewService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadPendingReviews();
  }

  loadPendingReviews(): void {
    this.isLoading = true;
    this.reviewService.getPendingReviews().subscribe({
      next: (response) => {
        this.pendingReviews = response.data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load pending reviews';
        this.isLoading = false;
      }
    });
  }

  approveReview(reviewId: number): void {
    this.reviewService.approveReview(reviewId).subscribe({
      next: () => {
        this.snackBar.open('Review approved successfully', 'Close', { duration: 3000 });
        this.loadPendingReviews();
      },
      error: (err) => {
        this.snackBar.open('Failed to approve review', 'Close', { duration: 3000 });
      }
    });
  }

  openRejectDialog(reviewId: number): void {
    const dialogRef = this.dialog.open(RejectReasonDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(reason => {
      if (reason !== undefined) {
        this.rejectReview(reviewId, reason);
      }
    });
  }

  rejectReview(reviewId: number, reason?: string): void {
    this.reviewService.rejectReview(reviewId, reason).subscribe({
      next: () => {
        this.snackBar.open('Review rejected successfully', 'Close', { duration: 3000 });
        this.loadPendingReviews();
      },
      error: (err) => {
        this.snackBar.open('Failed to reject review', 'Close', { duration: 3000 });
      }
    });
  }

  getStarIcons(rating: number): string[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating ? 'star' : 'star_border');
    }
    return stars;
  }

  getUserInitials(userName: string): string {
    return userName.charAt(0).toUpperCase();
  }

  getUserColor(userId: number): string {
    const colors = ['#FF5733', '#33FF57', '#3357FF', '#F033FF', '#FF33F0', '#33FFF0'];
    return colors[userId % colors.length];
  }
}
