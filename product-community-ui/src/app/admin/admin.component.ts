import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../_services/product.service';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatError, MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { ProductInfoDto } from '../_models/product.model';


@Component({
  selector: 'app-admin',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatButtonModule,
    MatTooltipModule,
    MatSelectModule,
    MatCardModule,
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {
   products: ProductInfoDto[] = [];
  filteredProducts: ProductInfoDto[] = [];
  searchForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private productService: ProductService,
    private fb: FormBuilder
  ) {
    this.searchForm = this.fb.group({
      searchTerm: [''],
      filterBy: ['name'],
    });
  }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.productService.getAllProductsWithReviewStats().subscribe({
      next: (response) => {
        this.products = response.data;
        this.filteredProducts = [...this.products];
        console.log(this.filteredProducts);

        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load products';
        this.isLoading = false;
      },
    });
  }

  searchProducts(): void {
    const searchTerm = this.searchForm
      .get('searchTerm')
      ?.value?.trim()
      .toLowerCase();
    const filterBy = this.searchForm.get('filterBy')?.value;

    if (!searchTerm) {
      this.filteredProducts = [...this.products];
      return;
    }

    this.filteredProducts = this.products.filter((product) => {
      if (filterBy === 'name') {
        return product.name.toLowerCase().includes(searchTerm);
      } else if (filterBy === 'code') {
        return product.code.toLowerCase().includes(searchTerm);
      } else if (filterBy === 'brand') {
        return product.brand.toLowerCase().includes(searchTerm);
      }
      return true;
    });
  }

  getStarIcon(starIndex: number, rating: number): string {
    if (!rating || rating === 0) {
      return 'star_border';
    }
    return starIndex <= Math.round(rating) ? 'star' : 'star_border';
  }


}
