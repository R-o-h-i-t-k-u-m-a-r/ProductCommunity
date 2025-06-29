import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  ProductDTO,
  ReviewDTO,
  ProductReviewRequest,
  ProductInfoDto,
} from '../_models/product.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  getAllProductsWithReviewStats(): Observable<ApiResponse<ProductInfoDto[]>> {
    return this.http.get<ApiResponse<ProductInfoDto[]>>(
      `${this.apiUrl}/product/all/info`
    );
  }

  getAllProducts(): Observable<ApiResponse<ProductDTO[]>> {
    return this.http.get<ApiResponse<ProductDTO[]>>(
      `${this.apiUrl}/product/all`
    );
  }

  getProductsWithApprovedReviews(): Observable<ApiResponse<ProductDTO[]>> {
    return this.http.get<ApiResponse<ProductDTO[]>>(
      `${this.apiUrl}/product/all/approve/review`
    );
  }

  getProductById(id: number): Observable<ApiResponse<ProductDTO>> {
    return this.http.get<ApiResponse<ProductDTO>>(
      `${this.apiUrl}/productId/${id}`
    );
  }

  searchProducts(
    name?: string,
    code?: string,
    brand?: string
  ): Observable<ApiResponse<ProductDTO[]>> {
    let params = new HttpParams();
    if (name) params = params.append('name', name);
    if (code) params = params.append('code', code);
    if (brand) params = params.append('brand', brand);

    return this.http.get<ApiResponse<ProductDTO[]>>(`${this.apiUrl}/search`, {
      params,
    });
  }

  getProductReviews(
    productId: number,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<ReviewDTO[]>> {
    return this.http.get<ApiResponse<ReviewDTO[]>>(
      `${environment.apiUrl}/reviews/product/${productId}`,
      { params: { page: page.toString(), size: size.toString() } }
    );
  }

  getProductReviewStats(
    productId: number
  ): Observable<ApiResponse<{ averageRating: number; reviewCount: number }>> {
    return this.http.get<
      ApiResponse<{ averageRating: number; reviewCount: number }>
    >(`${environment.apiUrl}/reviews/product/${productId}/stats`);
  }

  addProduct(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/add`, formData, {
      reportProgress: true,
      observe: 'response',
    });
  }
}
