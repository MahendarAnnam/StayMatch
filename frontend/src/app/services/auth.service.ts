import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RefreshTokenRequest,
  RefreshTokenResponse,
  RegisterRequest
} from '../models/auth.model';
import { TokenStorageService } from './token-storage.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  /**
   * Emits the current user (or null when logged out).
   * Components can subscribe to react to auth-state changes.
   */
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  readonly currentUser$: Observable<AuthUser | null> = this.currentUserSubject.asObservable();

  constructor(
    private readonly http: HttpClient,
    private readonly tokenStorage: TokenStorageService,
    private readonly router: Router
  ) {
    // Re-emit persisted user on service creation (e.g. page refresh)
    const stored = this.tokenStorage.getUser();
    if (stored) {
      this.currentUserSubject.next(stored);
    }
  }

  // ── Public API ──────────────────────────────────────────────────────

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, request)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  /**
   * Exchanges the stored refresh token for a new short-lived access token.
   *
   * Called by the AuthInterceptor on 401 — should NOT be called manually
   * from components in most cases.
   */
  refreshToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    const body: RefreshTokenRequest = { refreshToken: refreshToken ?? '' };

    return this.http
      .post<RefreshTokenResponse>(`${this.baseUrl}/refresh`, body)
      .pipe(
        tap((res) => this.tokenStorage.setAccessToken(res.accessToken))
      );
  }

  logout(): void {
    this.tokenStorage.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  // ── Convenience Getters ─────────────────────────────────────────────

  getAccessToken(): string | null {
    return this.tokenStorage.getAccessToken();
  }

  isLoggedIn(): boolean {
    return this.tokenStorage.isLoggedIn();
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    return this.currentUserSubject.value?.role === role;
  }

  // ── Private Helpers ─────────────────────────────────────────────────

  private handleAuthResponse(res: AuthResponse): void {
    const user: AuthUser = {
      userId: res.userId,
      email: res.email,
      role: res.role,
      fullName: res.fullName,
      phoneNumber: res.phoneNumber
    };
    this.tokenStorage.saveSession(res.token, res.refreshToken, user);
    this.currentUserSubject.next(user);
  }
}

