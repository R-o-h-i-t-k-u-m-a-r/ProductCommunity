import {
  HttpClient,
  HttpErrorResponse,
  HttpEvent,
  HttpEventType,
  HttpHeaders,
  HttpParams,
} from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, throwError } from 'rxjs';
import { LoginRequest, UserUpdateRequest } from '../_models/login-request';
import { UserSignUpRequest } from '../_models/user-sign-up';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserAuthService } from './user-auth.service';
import { ApiResponse } from '../_models/api-response';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private httpclient = inject(HttpClient);
  PATH_OF_API = 'http://localhost:9191/api/v1';
  private apiUrl = environment.apiUrl;

  requestHeader = new HttpHeaders({ 'No-Auth': 'True' });

  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    private userAuth: UserAuthService
  ) {}

  public healthCheck() {
    return this.httpclient.get(this.PATH_OF_API + '/public/health-check');
  }

  public login(loginRequest: LoginRequest): Observable<any> {
    return this.httpclient.post(`${this.apiUrl}/public/login`, loginRequest, {
      withCredentials: true,
    });
  }

  public signUp(signUpRequest: UserSignUpRequest): Observable<ApiResponse> {
    return this.httpclient.post<ApiResponse>(
      `${this.apiUrl}/public/signup`,
      signUpRequest
    );
  }

  public currentUser(): Observable<any> {
    return this.httpclient.get(`${this.apiUrl}/users/user`, {
      withCredentials: true,
    });
  }

  public updateUserInfo(
    updateUser: UserUpdateRequest
  ): Observable<UserUpdateRequest> {
    return this.httpclient.put<UserUpdateRequest>(
      `${this.apiUrl}/users/update`,
      updateUser,
      {
        withCredentials: true,
      }
    );
  }

  public allUsers() {
    return this.httpclient.get(this.PATH_OF_API + '/admin/all-user');
  }

  handleGoogleCallback(code: string) {
    this.httpclient
      .get<any>(`http://localhost:9191/auth/google/callback?code=${code}`)
      .subscribe({
        next: (response) => {
          // Save token and user data
          //localStorage.setItem('token', response.token);
          this.userAuth.setToken(response.token);
          this.userAuth.setRoles('NORMAL');
          const userId: number = Number(response.userId);
          this.userAuth.setUserId(userId);
          this.userAuth.setUserName(response.email);
          localStorage.setItem(
            'user',
            JSON.stringify({
              email: response.email,
              firstName: response.firstName,
              lastName: response.lastName,
            })
          );

          this.snackBar.open('Logged in successfully!', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar'],
          });

          this.router.navigate(['/']);
        },
        error: (err) => {
          this.snackBar.open('Login failed. Please try again.', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
          this.router.navigate(['/login']);
        },
      });
  }

  public addUserImage(formData: FormData, userId: number): Observable<any> {
    // Create HttpParams
    const params = new HttpParams().set('userId', userId.toString());
    return this.httpclient
      .post(`${this.apiUrl}/user/upload`, formData, {
        params: params,
        withCredentials: true,
        reportProgress: true,
        observe: 'events',
      })
      .pipe(
        map((event) => this.getEventMessage(event)),
        catchError(this.handleError)
      );
  }

  deleteUserImage(imageId: number): Observable<any> {
    return this.httpclient.delete(
      `${this.apiUrl}/user/image/${imageId}/delete`,
      {
        withCredentials: true,
      }
    );
  }

  public updateUserImage(formData: FormData, imageId: number): Observable<any> {
    return this.httpclient
      .put(`${this.apiUrl}/user/image/${imageId}/update`, formData, {
        withCredentials: true,
        reportProgress: true,
        observe: 'events',
      })
      .pipe(
        map((event) => this.getEventMessage(event)),
        catchError(this.handleError)
      );
  }

  private getEventMessage(event: HttpEvent<any>): string {
    switch (event.type) {
      case HttpEventType.UploadProgress:
        const percentDone = Math.round(
          (100 * event.loaded) / (event.total || 1)
        );
        return `Uploading: ${percentDone}%`;
      case HttpEventType.Response:
        return event.body;
      default:
        return `Event: ${event.type}`;
    }
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Upload error:', error);
    return throwError(() => new Error('Upload failed. Please try again.'));
  }
}
