import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class UserAuthService {
  private tokenExpirationTimer: any;

  constructor() {}

  public setRoles(role: string) {
    localStorage.setItem('roles', role);
  }

  public getRoles(): string {
    const roles = localStorage.getItem('roles') || '';
    return roles;
  }

  public setToken(jwtToken: string) {
    localStorage.setItem('jwtToken', jwtToken);
  }

  public getToken(): string {
    return localStorage.getItem('jwtToken') || '';
  }

  public clear() {
    localStorage.clear();
  }

  public isLoggedIn(): boolean {
    return this.getRoles().length > 0 && this.getToken().trim() !== '';
  }

  public isAdmin() {
    const roles: string = this.getRoles();
    return roles === 'ROLE_ADMIN';
  }

  public isUser() {
    const roles: string = this.getRoles();
    return roles === 'ROLE_USER';
  }

  public setUserName(username: string) {
    localStorage.setItem('userName', username);
  }

  public getUserName(): string {
    return localStorage.getItem('userName') || '';
  }

  public setUserImage(imageUrl: string | undefined | null): void {
    if (imageUrl) {
      localStorage.setItem('userImageUrl', imageUrl);
    } else {
      localStorage.removeItem('userImageUrl');
    }
  }

  public getUserImage(): string {
    const url = localStorage.getItem('userImageUrl');
    return url && url !== 'undefined' ? url : '';
  }

  getTokenExpiration(): number | null {
    const token = this.getToken();
    if (!token) return null;

    const jwtData = token.split('.')[1];
    const decodedJwtJsonData = window.atob(jwtData);
    const decodedJwtData = JSON.parse(decodedJwtJsonData);

    return decodedJwtData.exp * 1000; // Convert to milliseconds
  }

  setAutoLogout(expirationDuration: number) {
    this.tokenExpirationTimer = setTimeout(() => {
      this.clear();
    }, expirationDuration);
  }

  clearAutoLogout() {
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
      this.tokenExpirationTimer = null;
    }
  }

  setUserId(userId: number) {
    localStorage.setItem('userId', userId.toString());
  }

  getUserId(): number | null {
    const userIdStr = localStorage.getItem('userId');
    return userIdStr !== null ? Number(userIdStr) : null;
  }
}
