import { Component, OnInit } from '@angular/core';
import {
  ProductDTO,
  ProductReviewRequest,
  ReviewDTO,
  UserDTO,
} from '../_models/product.model';
import { ProductService } from '../_services/product.service';
import { ReviewService } from '../_services/review.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { UserAuthService } from '../_services/user-auth.service';
import { UserService } from '../_services/user.service';

@Component({
  selector: 'app-product-details',
  imports: [
    CommonModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatButtonModule,
    FormsModule
  ],
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.css',
})
export class ProductDetailsComponent implements OnInit {
  product: ProductDTO | null = null;
  isLoading = true;
  errorMessage = '';

  currentUser: UserDTO | null = null;
  isCommentExpanded = false;
  reviewRating = 0;
  reviewTitle = '';
  reviewContent = '';
  isSubmitting = false;
  isUserLoggedIn = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private authService: UserAuthService,
    private reveiwService: ReviewService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.params['id'];
    this.loadProductDetails(productId);
    this.isUserLoggedIn = this.authService.isLoggedIn();
    this.fetchCurrentUser();
  }

  loadProductDetails(productId: number): void {
    this.isLoading = true;
    this.productService.getProductById(productId).subscribe({
      next: (response) => {
        this.product = response.data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load product details';
        this.isLoading = false;
      },
    });
  }

  getApprovedReviews(): ReviewDTO[] {
    return (
      this.product?.reviews?.filter(
        (review) => review.reviewStatus === 'APPROVED'
      ) || []
    );
  }

  getStarIcons(rating: number): string[] {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const stars = [];

    
    for (let i = 0; i < fullStars; i++) {
      stars.push('star');
    }

   
    if (hasHalfStar) {
      stars.push('star_half');
    }

    
    const remainingStars = 5 - stars.length;
    for (let i = 0; i < remainingStars; i++) {
      stars.push('star_border');
    }

    return stars;
  }

  getUserInitials(user: UserDTO): string {
    if (!user.firstName && !user.lastName) {
      return user.userName.charAt(0).toUpperCase();
    }
    return `${user.firstName?.charAt(0) || ''}${
      user.lastName?.charAt(0) || ''
    }`.toUpperCase();
  }

  getUserColor(user: UserDTO): string {
    // Generate a consistent color based on user ID or name
    const colors = [
      '#FF5733',
      '#33FF57',
      '#3357FF',
      '#F033FF',
      '#FF33F0',
      '#33FFF0',
    ];
    const hash = user.id ? user.id : (user.firstName + user.lastName).length;
    return colors[hash % colors.length];
  }

  fetchCurrentUser(): void{
    if(this.isUserLoggedIn){
      this.userService.currentUser().subscribe({
        next: (response:any)=>{
          this.currentUser = response.data;
        },
        error: (error)=>{
          console.log("Occured some while fetching current user");
          
        }
      })
    }
  }


  toggleCommentSection(): void {
    this.isCommentExpanded = !this.isCommentExpanded;
    if (!this.isCommentExpanded) {
      this.resetReviewForm();
    }
  }

  setRating(rating: number): void {
    this.reviewRating = rating;
  }

  submitReview(): void {
    if (!this.product || !this.currentUser) return;

    this.isSubmitting = true;
    
    const reviewRequest: ProductReviewRequest = {
      productId: this.product.id,
      rating: this.reviewRating,
      title: this.reviewTitle,
      content: this.reviewContent
    };

    this.reveiwService.createReview(reviewRequest).subscribe({
      next: (response) => {
        // Reload product to show the new review
        this.loadProductDetails(this.product!.id);
        this.resetReviewForm();
        this.isCommentExpanded = false;
        this.isSubmitting = false;
      },
      error: (err) => {
        this.errorMessage = 'You can not post review twice, instead you can update or delete';
        this.isSubmitting = false;
        this.resetReviewForm();
      }
    });
  }

  private resetReviewForm(): void {
    this.reviewRating = 0;
    this.reviewTitle = '';
    this.reviewContent = '';
  }
}
