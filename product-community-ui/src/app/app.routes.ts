import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegistrationComponent } from './registration/registration.component';
import { AdminComponent } from './admin/admin.component';
import { UserComponent } from './user/user.component';
import { adminGuard } from './_auth/admin.guard';
import { normalGuard } from './_auth/normal.guard';

export const routes: Routes = [
  {
    path: 'admin',
    component: AdminComponent,
    pathMatch: 'full',
    canActivate: [adminGuard],
  },
  {
    path: 'user',
    component: UserComponent,
    pathMatch: 'full',
    canActivate: [normalGuard],
  },
  { path: 'login', component: LoginComponent, pathMatch: 'full' },
  { path: 'register', component: RegistrationComponent, pathMatch: 'full' },
];
