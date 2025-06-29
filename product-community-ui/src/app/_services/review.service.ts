import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, Observable } from 'rxjs';
import {
  ApiResponse,
  CreateReviewRequest,
  Page,
  ProductReviewRequest,
  ReviewDTO,
  ReviewRequestDTO,
  ReviewResponse,
} from '../_models/product.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  createReview(
    reviewRequest: ProductReviewRequest
  ): Observable<ApiResponse<ReviewDTO>> {
    return this.http.post<ApiResponse<ReviewDTO>>(this.apiUrl, reviewRequest);
  }

  getUserReviews(): Observable<ApiResponse<ReviewDTO[]>> {
    return this.http.get<ApiResponse<ReviewDTO[]>>(`${this.apiUrl}/user`);
  }

  getProductReviews(productId: Number): Observable<ApiResponse<ReviewDTO[]>> {
    return this.http.get<ApiResponse<ReviewDTO[]>>(
      `${this.apiUrl}/product/${productId}`
    );
  }

  getPendingReviews(): Observable<ApiResponse<ReviewResponse[]>> {
    return this.http.get<ApiResponse<ReviewResponse[]>>(
      `${this.apiUrl}/all/pending`
    );
  }

  approveReview(reviewId: number): Observable<ApiResponse<ReviewResponse>> {
    return this.http.get<ApiResponse<ReviewResponse>>(
      `${this.apiUrl}/${reviewId}/approve`
    );
  }

  rejectReview(
    reviewId: number,
    reason?: string
  ): Observable<ApiResponse<ReviewResponse>> {
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.get<ApiResponse<ReviewResponse>>(
      `${this.apiUrl}/${reviewId}/reject`,
      { params }
    );
  }

  getUserReviewsPaginated(
    page: number = 0,
    size: number = 10
  ): Observable<Page<ReviewResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<ReviewResponse>>(`${this.apiUrl}/user`, {
      params,
    });
  }

  requestReview(request: CreateReviewRequest): Observable<ReviewRequestDTO> {
    return this.http
      .post<ReviewRequestDTO>(`${this.apiUrl}/request-review`, request)
      .pipe(
        catchError((error) => {
          if (error.status === 409) {
            throw new Error('Product with this code already exists');
          }
          throw new Error('Failed to submit review request. Please try again.');
        })
      );
  }

  getPendingReviewRequests(): Observable<ApiResponse<ReviewRequestDTO[]>> {
    return this.http.get<ApiResponse<ReviewRequestDTO[]>>(
      `${this.apiUrl}/request-review/all/pending`
    );
  }

  approveReviewRequest(
    reviewId: number
  ): Observable<ApiResponse<ReviewRequestDTO>> {
    return this.http.get<ApiResponse<ReviewRequestDTO>>(
      `${this.apiUrl}/request-review/${reviewId}/approve`
    );
  }

  rejectReviewRequest(
    reviewId: number,
    reason?: string
  ): Observable<ApiResponse<ReviewRequestDTO>> {
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.get<ApiResponse<ReviewRequestDTO>>(
      `${this.apiUrl}/request-review/${reviewId}/reject`,
      { params }
    );
  }
}
