import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from "./header/header.component";
import { UserAuthService } from './_services/user-auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'product-community-ui';

  constructor(private authService: UserAuthService) {}
  ngOnInit() {
    this.setupAutoLogout();
  }

  ngOnDestroy() {
    this.authService.clearAutoLogout();
  }

  private setupAutoLogout() {
    const expirationDate = this.authService.getTokenExpiration();
    if (expirationDate) {
      const now = new Date().getTime();
      const expiresIn = expirationDate - now;

      if (expiresIn > 0) {
        this.authService.setAutoLogout(expiresIn);
      } else {
        this.authService.clear();
      }
    }
  }
}
