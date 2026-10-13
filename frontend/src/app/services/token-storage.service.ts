import { Injectable } from '@angular/core';
import { AuthUser } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'sm_access_token';
const REFRESH_TOKEN_KEY = 'sm_refresh_token';
const USER_KEY = 'sm_user';

/**
 * Centralised token storage utility.
 *
 * Keeps the access token in memory (volatile) with a localStorage fallback
 * so that a full-page reload still preserves the session, while the refresh
 * token is persisted in localStorage for the 7-day lifespan.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  // In-memory access token — preferred over localStorage for security
  private accessToken: string | null = null;

  constructor() {
    // Rehydrate from localStorage on app bootstrap (e.g. after page refresh)
    this.accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  // ── Access Token ────────────────────────────────────────────────────

  getAccessToken(): string | null {
    return this.accessToken;
  }

  setAccessToken(token: string): void {
    this.accessToken = token;
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  }

  // ── Refresh Token ───────────────────────────────────────────────────

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  }

  // ── User Metadata ───────────────────────────────────────────────────

  getUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  }

  setUser(user: AuthUser): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  // ── Convenience ─────────────────────────────────────────────────────

  /** Persist both tokens + user info in one call (used after login / register). */
  saveSession(accessToken: string, refreshToken: string, user: AuthUser): void {
    this.setAccessToken(accessToken);
    this.setRefreshToken(refreshToken);
    this.setUser(user);
  }

  /** Returns true when an access token is available. */
  isLoggedIn(): boolean {
    return !!this.accessToken;
  }

  /** Wipe everything — called on logout or when refresh fails. */
  clear(): void {
    this.accessToken = null;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}

