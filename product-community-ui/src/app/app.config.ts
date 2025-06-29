import {
  ApplicationConfig,
  importProvidersFrom,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import {MatTooltipModule} from '@angular/material/tooltip';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { authInterceptor } from './_auth/auth.interceptor';
import { MatDividerModule } from '@angular/material/divider';
import {MatSelectModule} from '@angular/material/select';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptor])),

    importProvidersFrom(
      MatToolbarModule,
      MatMenuModule,
      MatFormFieldModule,
      MatDialogModule,
      MatSnackBarModule,
      MatDialogModule,
      MatCardModule,
      MatIconModule,
      MatInputModule,
      MatButtonModule,
      MatProgressSpinnerModule,
      MatTableModule,
      MatListModule,
      MatChipsModule,
      MatDividerModule,
      MatTooltipModule,
      MatSelectModule
    ),
  ],
};
