import { NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { UserService } from '../_services/user.service';
import { Router, RouterLink } from '@angular/router';
import { LoginRequest } from '../_models/login-request';
import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';
import { UserAuthService } from '../_services/user-auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  imports: [FormsModule, NgIf, MatIconModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  animations: [
    trigger('fadeInOut', [
      state('void', style({ opacity: 0 })),
      transition(':enter', [animate('300ms ease-in', style({ opacity: 1 }))]),
      transition(':leave', [animate('300ms ease-out', style({ opacity: 0 }))]),
    ]),
  ],
})
export class LoginComponent {
  isLoading = false;
  errorMessage: string | null = null;
  private errorTimeout: any;
  hidePassword: boolean = true;

  constructor(
    private userService: UserService,
    private router: Router,
    private userAuth: UserAuthService
  ) {
    //
  }

  addDetails(loginForm: NgForm) {
    if (loginForm.invalid) return;

    this.isLoading = true;
    //this.errorMessage = null;
    this.clearError(); // Clear any existing error

    const loginRequest: LoginRequest = {
      userName: loginForm.value.userName,
      password: loginForm.value.password,
    };

    this.userService.login(loginRequest).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        this.isLoading = false;
        // Handle successful login (e.g., store token, redirect)
        this.userAuth.setToken(response.jwtToken);

        const userRole: string = response.userDto.roles[0].name;
        this.userAuth.setRoles(userRole);

        const userId: number = Number(response.userDto.id);
        this.userAuth.setUserId(userId);

        this.userAuth.setUserName(loginRequest.userName);

        this.userAuth.setUserImage(response.userDto.userImage?.downloadUrl);

        // Set auto logout
        const expirationDate = this.userAuth.getTokenExpiration();
        if (expirationDate) {
          const expiresIn = expirationDate - new Date().getTime();
          this.userAuth.setAutoLogout(expiresIn);
        }

        if (userRole === 'ROLE_ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/user']);
        }
      },
      error: (error) => {
        console.error('Login failed', error);
        this.isLoading = false;
        //this.errorMessage = error.error?.message || 'Login failed. Please try again.';
        this.showError(
          error.error?.message || 'Login failed. Please try again.'
        );
      },
    });
  }

  private showError(message: string) {
    this.errorMessage = message;
    // Auto-dismiss after 5 seconds (5000 milliseconds)
    this.errorTimeout = setTimeout(() => {
      this.clearError();
    }, 5000);
  }

  public clearError() {
    this.errorMessage = null;
    if (this.errorTimeout) {
      clearTimeout(this.errorTimeout);
    }
  }

  // Clear error when component is destroyed
  ngOnDestroy() {
    this.clearError();
  }

  gethealth() {
    this.userService.healthCheck().subscribe({
      next: (response: any) => {
        console.log('api fetch sucessfull', response);
        alert(response.message);
      },
      error: (error) => {
        console.log('api fetch failled', error);
        alert('application is not running');
      },
    });
  }

  loginWithGoogle() {
  // TODO: implement Google login flow
}

loginWithFacebook() {
  // TODO: implement Facebook login flow
}

loginWithGithub() {
  // TODO: implement GitHub login flow
}
}
