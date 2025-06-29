import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReviewService } from '../_services/review.service';
import { CreateReviewRequest } from '../_models/product.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-review-request',
  imports: [CommonModule,
    ReactiveFormsModule],
  templateUrl: './review-request.component.html',
  styleUrl: './review-request.component.css',
})
export class ReviewRequestComponent {
  reviewRequestForm: FormGroup;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private reviewRequestService: ReviewService
  ) {
    this.reviewRequestForm = this.fb.group({
      productName: ['', Validators.required],
      productCode: ['', Validators.required],
      productBrand: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.reviewRequestForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const request: CreateReviewRequest = {
      productName: this.reviewRequestForm.value.productName,
      productCode: this.reviewRequestForm.value.productCode,
      productBrand: this.reviewRequestForm.value.productBrand,
    };

    this.reviewRequestService.requestReview(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Review request submitted successfully!';
        this.reviewRequestForm.reset();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message;
      },
    });
  }
}
