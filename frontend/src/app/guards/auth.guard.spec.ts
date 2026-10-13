import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { AuthUser } from '../models/auth.model';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getCurrentUser']);
    mockRouter = jasmine.createSpyObj('Router', ['createUrlTree']);
    mockRouter.createUrlTree.and.returnValue('/login' as any);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    guard = TestBed.inject(AuthGuard);
  });

  function createRouteSnapshot(data: Record<string, any> = {}): ActivatedRouteSnapshot {
    return { data } as unknown as ActivatedRouteSnapshot;
  }

  // ── Authentication checks ─────────────────────────────────────────

  it('should redirect to /login when user is not logged in', () => {
    mockAuthService.isLoggedIn.and.returnValue(false);

    const result = guard.canActivate(createRouteSnapshot());

    expect(result).toBe('/login' as any);
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  it('should allow access when user is logged in and no roles required', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);

    const result = guard.canActivate(createRouteSnapshot());

    expect(result).toBeTrue();
  });

  // ── Role-based checks ────────────────────────────────────────────

  it('should allow access when user has the required role', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockAuthService.getCurrentUser.and.returnValue({
      userId: 1,
      email: 'owner@test.com',
      role: 'PG_OWNER'
    } as AuthUser);

    const result = guard.canActivate(createRouteSnapshot({ roles: ['PG_OWNER', 'ADMIN'] }));

    expect(result).toBeTrue();
  });

  it('should redirect when user does not have the required role', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockRouter.createUrlTree.and.returnValue('/pg-search' as any);
    mockAuthService.getCurrentUser.and.returnValue({
      userId: 1,
      email: 'tenant@test.com',
      role: 'TENANT'
    } as AuthUser);

    const result = guard.canActivate(createRouteSnapshot({ roles: ['PG_OWNER', 'ADMIN'] }));

    expect(result).toBe('/pg-search' as any);
    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/pg-search']);
  });

  it('should redirect when user is null but route requires roles', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockRouter.createUrlTree.and.returnValue('/pg-search' as any);
    mockAuthService.getCurrentUser.and.returnValue(null);

    const result = guard.canActivate(createRouteSnapshot({ roles: ['ADMIN'] }));

    expect(result).toBe('/pg-search' as any);
  });

  it('should allow access when roles array is empty', () => {
    mockAuthService.isLoggedIn.and.returnValue(true);

    const result = guard.canActivate(createRouteSnapshot({ roles: [] }));

    expect(result).toBeTrue();
  });
});

