import { Component, HostListener } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UserAuthService } from '../_services/user-auth.service';
import { NgIf } from '@angular/common';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    NgIf,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  userName: String = '';
  hasScrolled: boolean = false;
 

  constructor(private userAuth: UserAuthService, private router: Router) {}
  ngOnInit() {
    this.getUserName();
  }

  get avatarUrl(): string | null {
    const url = this.userAuth.getUserImage(); 
    return url && url.trim() !== '' ? url : null;
    
  }

  public getUserName() {
    return (this.userName = this.userAuth.getUserName());
  }

  public isLogged() {
    return this.userAuth.isLoggedIn();
  }

  public logout() {
    this.userAuth.clear();
    this.router.navigate(['/login']);
  }

  public isAdminRole() {
    return this.userAuth.isAdmin();
  }

  public isUserRole() {
    return this.userAuth.isUser();
  }

  public onClick() {
    //routerLink="/admin/products/add"
    //[state] = "{ mode: 'delete' }"
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.hasScrolled = window.scrollY > 0;
  }
}
