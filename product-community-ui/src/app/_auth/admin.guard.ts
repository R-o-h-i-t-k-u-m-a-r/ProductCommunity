import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserAuthService } from '../_services/user-auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(UserAuthService)
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.getRoles() === "ROLE_ADMIN") {
    return true;
  }
  
  router.navigate(['/login']);
  return false;
};