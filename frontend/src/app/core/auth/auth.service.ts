import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  AuthResponse,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
} from '../models/auth.model';
import { User } from '../models/user.model';
import { ApiService } from '../services/api.service';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly tokenStorage = inject(TokenStorageService);

  private readonly currentUserSignal = signal<User | null>(null);
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.tokenStorage.accessToken() !== null);
  readonly currentRole = computed(() => this.currentUserSignal()?.role ?? null);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', request).pipe(
      tap((res) => {
        this.tokenStorage.setTokens(res.accessToken, res.refreshToken);
        this.currentUserSignal.set(res.user);
      })
    );
  }

  register(request: RegisterRequest): Observable<void> {
    return this.api.post<void>('/auth/register', request);
  }

  activate(token: string): Observable<void> {
    return this.api.get<void>('/auth/activate', { token });
  }

  resendActivation(email: string): Observable<void> {
    return this.api.post<void>('/auth/resend-activation', undefined, { email });
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.api.post<void>('/auth/forgot-password', request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.api.post<void>('/auth/reset-password', request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.api.patch<void>('/auth/change-password', request);
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.api.post<AuthResponse>('/auth/refresh', { refreshToken }).pipe(
      tap((res) => {
        this.tokenStorage.setTokens(res.accessToken, res.refreshToken);
        this.currentUserSignal.set(res.user);
      })
    );
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.api.post<void>('/auth/logout', { refreshToken }).pipe(
      tap(() => this.clearSession())
    );
  }

  clearSession(): void {
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }

  loadCurrentUser(): Observable<User> {
    return this.api.get<User>('/users/me').pipe(tap((user) => this.currentUserSignal.set(user)));
  }

  setCurrentUser(user: User): void {
    this.currentUserSignal.set(user);
  }
}
