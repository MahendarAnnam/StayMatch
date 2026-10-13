import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';
import { environment } from '../../environments/environment';

/**
 * HTTP interceptor that:
 *  1. Attaches the `Authorization: Bearer <accessToken>` header to every
 *     outgoing request (when a token is available).
 *  2. Catches 401 responses and transparently refreshes the access token
 *     using the stored refresh token, then retries the original request.
 *  3. If the refresh itself fails → clears the session and redirects to login.
 *
 * Implements the "refresh-lock" pattern so that multiple concurrent 401s
 * only trigger a single refresh call; the rest queue up and are replayed
 * once the new token is available.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  /** Whether a token refresh is already in-flight. */
  private isRefreshing = false;

  /**
   * Emits `null` while refreshing, then the new token once ready.
   * Queued requests subscribe and wait for the non-null emission.
   */
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private readonly authService: AuthService,
    private readonly tokenStorage: TokenStorageService
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    // Skip external URLs (e.g. Nominatim geocoding) and the refresh endpoint
    if (!req.url.startsWith(environment.apiBaseUrl) || this.isRefreshUrl(req.url)) {
      return next.handle(req);
    }

    // Clone request with current access token
    const authedReq = this.addToken(req);

    return next.handle(authedReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && this.tokenStorage.getRefreshToken()) {
          return this.handle401(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  // ── Private Helpers ─────────────────────────────────────────────────

  /** Adds the Bearer token header if available. */
  private addToken(req: HttpRequest<unknown>): HttpRequest<unknown> {
    const token = this.tokenStorage.getAccessToken();
    if (!token) {
      return req;
    }
    return req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  /**
   * Handles a 401 response:
   *  – If no refresh is in progress → starts one, then retries.
   *  – If a refresh is already running → waits for the new token, then retries.
   */
  private handle401(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);              // signal "refreshing"

      return this.authService.refreshToken().pipe(
        switchMap((res) => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(res.accessToken); // unblock queued requests
          return next.handle(this.addToken(req));
        }),
        catchError((err) => {
          this.isRefreshing = false;
          this.authService.logout();                      // refresh failed → force re-login
          return throwError(() => err);
        })
      );
    }

    // Another request already triggered a refresh — wait for it
    return this.refreshTokenSubject.pipe(
      filter((token): token is string => token !== null), // wait until non-null
      take(1),
      switchMap(() => next.handle(this.addToken(req)))
    );
  }

  /** Returns true if the URL is the refresh endpoint (to skip interception). */
  private isRefreshUrl(url: string): boolean {
    return url.includes(`${environment.apiBaseUrl}/auth/refresh`);
  }
}

