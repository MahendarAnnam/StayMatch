import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Route guard that ensures:
 *  1. The user is authenticated (access token present).
 *  2. Optionally, the user has one of the allowed roles defined in
 *     route data: `{ data: { roles: ['PG_OWNER', 'ADMIN'] } }`.
 *
 * If the check fails the user is redirected to `/login`.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/login']);
    }

    const requiredRoles: string[] | undefined = route.data['roles'];
    if (requiredRoles && requiredRoles.length > 0) {
      const user = this.authService.getCurrentUser();
      if (!user || !requiredRoles.includes(user.role)) {
        return this.router.createUrlTree(['/pg-search']);   // redirect to a safe public page
      }
    }

    return true;
  }
}

