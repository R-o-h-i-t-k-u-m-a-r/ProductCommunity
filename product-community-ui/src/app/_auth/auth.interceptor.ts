import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from '@angular/core';
import { UserAuthService } from "../_services/user-auth.service";
import { Router } from "@angular/router";
import { throwError } from "rxjs";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(UserAuthService)
  const router = inject(Router);
  
  const token = authService.getToken();
  console.log("inside interceptor");
  if (token) {
    // Check if token is expired
    const expirationDate = authService.getTokenExpiration();
    if (expirationDate && Date.now() > expirationDate) {
      authService.clear();
      router.navigate(['/login']);
      return throwError(() => new Error('Token expired'));
    }
    
    const autReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(autReq);
  }
  
  return next(req);
};