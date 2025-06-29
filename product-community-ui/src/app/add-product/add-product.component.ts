import { ChangeDetectorRef, Component } from '@angular/core';

import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { ProductService } from '../_services/product.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-add-product',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css',
})
export class AddProductComponent {
  productForm: FormGroup;
  selectedFiles: File[] = [];
  isSubmitting = false;

   imagePreviews: string[] = [];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    public router: Router,
    private snackBar: MatSnackBar,
    private cdRef: ChangeDetectorRef
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      brand: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  // Method to create image preview URLs
  getImagePreview(file: File): string {
    // Return a blob URL for the file
    return URL.createObjectURL(file);
  }

  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files) {
      // Clear previous previews
      this.clearPreviews();
      
      this.selectedFiles = Array.from(files);
      this.imagePreviews = this.selectedFiles.map(file => this.getImagePreview(file));
      
      // Run change detection after async operation
      this.cdRef.detectChanges();
    }
  }

  removeImage(index: number): void {
    // Revoke the specific preview URL
    URL.revokeObjectURL(this.imagePreviews[index]);
    this.selectedFiles.splice(index, 1);
    this.imagePreviews.splice(index, 1);
  }

  private clearPreviews(): void {
    this.imagePreviews.forEach(url => URL.revokeObjectURL(url));
    this.imagePreviews = [];
  }

  ngOnDestroy(): void {
    this.clearPreviews();
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    
    const formData = new FormData();
    formData.append('product', JSON.stringify(this.productForm.value));
    
    this.selectedFiles.forEach(file => {
      formData.append('images', file);
    });

    this.productService.addProduct(formData).subscribe({
      next: (response) => {
        // Clean up all object URLs
        this.selectedFiles.forEach(file => {
          URL.revokeObjectURL(this.getImagePreview(file));
        });
        
        this.snackBar.open('Product added successfully!', 'Close', {
          duration: 3000
        });
        this.router.navigate(['/']);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        let errorMessage = 'An error occurred while adding the product';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  // Clean up object URLs when component is destroyed
  // ngOnDestroy(): void {
  //   this.selectedFiles.forEach(file => {
  //     URL.revokeObjectURL(this.getImagePreview(file));
  //   });
  // }
}
