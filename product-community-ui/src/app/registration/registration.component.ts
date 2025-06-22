import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatError, MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserSignUpRequest } from '../_models/user-sign-up';
import { UserService } from '../_services/user.service';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-registration',
  imports: [
    MatCardModule,
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinner,
    MatDividerModule,
    MatButtonModule,
    MatTooltipModule
  ],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class RegistrationComponent {
  registrationForm: FormGroup;
  isLoading = false;
  hidePassword: boolean = true;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private userServie: UserService,
    private router: Router
  ) {
    this.registrationForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      userName: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit(): void {
    if (this.registrationForm.valid) {
      this.isLoading = true;
      //const formData = this.registrationForm.value;
      const userSignUpRequest: UserSignUpRequest = {
        firstName: this.registrationForm.value.firstName,
        lastName: this.registrationForm.value.lastName,
        userName: this.registrationForm.value.userName,
        password: this.registrationForm.value.password,
      };

      this.userServie.signUp(userSignUpRequest).subscribe({
        next: (response) => {
          this.snackBar.open('Registration successful!', 'Close', {
            duration: 3000,
          });
          //console.log("User Account Created: ",response.data);

          this.router.navigate(['/login']); // Redirect to login page
        },
        error: (error) => {
          this.snackBar.open(
            error.error?.message || 'Registration failed',
            'Close',
            { duration: 3000 }
          );
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
    }
  }

  startGoogleSignIn() {
    this.isLoading = true;
    this.http
      .get<string>('http://localhost:9191/auth/google/auth-url', {
        responseType: 'text' as 'json',
      })
      .subscribe({
        next: (authUrl) => {
          window.location.href = authUrl; // Redirect to Google
        },
        error: (err) => {
          this.snackBar.open('Failed to initiate Google Sign-In', 'Close', {
            duration: 3000,
          });
          this.isLoading = false;
        },
      });
  }

  startGithubSignIn() {
    // Implement GitHub OAuth
  }

  startFacebookSignIn() {
    // Implement GitHub OAuth
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
  }
}
