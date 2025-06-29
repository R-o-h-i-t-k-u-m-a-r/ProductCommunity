import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegistrationComponent } from './registration/registration.component';
import { AdminComponent } from './admin/admin.component';
import { UserComponent } from './user/user.component';
import { adminGuard } from './_auth/admin.guard';
import { normalGuard } from './_auth/normal.guard';
import { ProductListComponent } from './product-list/product-list.component';
import { AddProductComponent } from './add-product/add-product.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { HomePageComponent } from './home-page/home-page.component';
import { roleGuard } from './_auth/role.guard';
import { PendingReviewsComponent } from './pending-reviews/pending-reviews.component';
import { UserReviewsComponent } from './user-reviews/user-reviews.component';
import { ReviewRequestComponent } from './review-request/review-request.component';
import { PendingReviewRequestsComponent } from './pending-review-requests/pending-review-requests.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { CallbackComponent } from './callback/callback.component';

export const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
  },
  {
    path: 'products',
    component: ProductListComponent,
    pathMatch: 'full',
    canActivate: [normalGuard],
  },
  {
    path: 'products/:id',
    component: ProductDetailsComponent,
    pathMatch: 'full',
    canActivate: [normalGuard],
  },
  {
    path: 'user-reviews',
    component: UserReviewsComponent,
    pathMatch: 'full',
    canActivate: [normalGuard],
  },
  {
    path: 'review-request',
    component: ReviewRequestComponent,
    pathMatch: 'full',
    canActivate: [normalGuard],
  },
  {
    path: 'admin',
    component: AdminComponent,
    pathMatch: 'full',
    canActivate: [adminGuard],
  },
  {
    path: 'admin/products/add',
    component: AddProductComponent,
    pathMatch: 'full',
    canActivate: [adminGuard],
  },
  {
    path: 'admin/pending-reviews',
    component: PendingReviewsComponent,
    pathMatch: 'full',
    canActivate: [adminGuard],
  },
  {
    path: 'pending-review-requests',
    component: PendingReviewRequestsComponent,
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
   { path: 'auth/callback', component: CallbackComponent, pathMatch: 'full' },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
  },
  { path: '**', component: PageNotFoundComponent, pathMatch: 'full' },
];
