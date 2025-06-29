import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../_services/review.service';
import { Page, ReviewResponse } from '../_models/product.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-user-reviews',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatChipsModule
  ],
  templateUrl: './user-reviews.component.html',
  styleUrls: ['./user-reviews.component.css']
})
export class UserReviewsComponent implements OnInit {
  reviewsPage: Page<ReviewResponse> | null = null;
  isLoading = true;
  errorMessage = '';
  pageSize = 10;
  pageIndex = 0;

  constructor(
    private reviewService: ReviewService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUserReviews();
  }

  loadUserReviews(): void {
    this.isLoading = true;
    this.reviewService.getUserReviewsPaginated(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.reviewsPage = page;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load user reviews';
        this.isLoading = false;
        this.snackBar.open(this.errorMessage, 'Close', { duration: 3000 });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUserReviews();
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

  getStatusColor(status: string): string {
    switch(status) {
      case 'APPROVED': return 'primary';
      case 'PENDING': return 'accent';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }
}