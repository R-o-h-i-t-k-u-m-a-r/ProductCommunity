import { CanActivateFn, Router } from '@angular/router';
import { UserAuthService } from '../_services/user-auth.service';
import { inject } from '@angular/core';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(UserAuthService);
    const router = inject(Router);

    if (authService.isLoggedIn() && allowedRoles.includes(authService.getRoles())) {
      return true;
    }
    
    router.navigate(['/login']);
    return false;
  };
};
