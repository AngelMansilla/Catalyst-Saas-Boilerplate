import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import {
  AuthTokens,
  ForgotPasswordRequest,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
  User,
} from '../models/auth.models';
import { TokenService } from './token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  private readonly _currentUser = signal<User | null>(null);

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this._currentUser());

  private readonly API = '/api/auth';

  login(request: LoginRequest): Observable<AuthTokens> {
    return this.http.post<AuthTokens>(`${this.API}/login`, request).pipe(
      tap((tokens) => this.tokenService.saveTokens(tokens))
    );
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${this.API}/register`, request);
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http.post<void>(`${this.API}/logout`, { refreshToken }).pipe(
      tap(() => {
        this.tokenService.clearTokens();
        this._currentUser.set(null);
        this.router.navigate(['/auth/login']);
      })
    );
  }

  refresh(): Observable<AuthTokens> {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http
      .post<AuthTokens>(`${this.API}/refresh`, { refreshToken })
      .pipe(tap((tokens) => this.tokenService.saveTokens(tokens)));
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.API}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.API}/reset-password`, request);
  }

  loadCurrentUser(): Observable<User> {
    return this.http.get<User>('/api/users/me').pipe(
      tap((user) => this._currentUser.set(user))
    );
  }

  setCurrentUser(user: User): void {
    this._currentUser.set(user);
  }

  clearSession(): void {
    this.tokenService.clearTokens();
    this._currentUser.set(null);
  }
}
