import { Injectable, signal } from '@angular/core';

const ACCESS_TOKEN_KEY = 'kungo_access_token';
const REFRESH_TOKEN_KEY = 'kungo_refresh_token';

/**
 * Persistance des jetons JWT en localStorage (survit au rechargement de page) et
 * exposition réactive via signal pour que les guards/interceptors et le header
 * reflètent immédiatement tout changement d'état d'authentification.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  readonly accessToken = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));

  getAccessToken(): string | null {
    return this.accessToken();
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    this.accessToken.set(accessToken);
  }

  setAccessToken(accessToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    this.accessToken.set(accessToken);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.accessToken.set(null);
  }

  isAuthenticated(): boolean {
    return !!this.accessToken();
  }
}
