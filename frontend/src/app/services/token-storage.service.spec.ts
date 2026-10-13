import { TestBed } from '@angular/core/testing';
import { TokenStorageService } from './token-storage.service';
import { AuthUser } from '../models/auth.model';

describe('TokenStorageService', () => {
  let service: TokenStorageService;

  const testUser: AuthUser = {
    userId: 42,
    email: 'alice@example.com',
    role: 'TENANT'
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenStorageService);
  });

  afterAll(() => localStorage.clear());

  // ── Access Token ──────────────────────────────────────────────────

  it('should return null when no access token is stored', () => {
    expect(service.getAccessToken()).toBeNull();
  });

  it('should store and retrieve an access token', () => {
    service.setAccessToken('access-abc');
    expect(service.getAccessToken()).toBe('access-abc');
  });

  it('should persist access token to localStorage', () => {
    service.setAccessToken('access-xyz');
    expect(localStorage.getItem('sm_access_token')).toBe('access-xyz');
  });

  // ── Refresh Token ─────────────────────────────────────────────────

  it('should return null when no refresh token is stored', () => {
    expect(service.getRefreshToken()).toBeNull();
  });

  it('should store and retrieve a refresh token', () => {
    service.setRefreshToken('refresh-123');
    expect(service.getRefreshToken()).toBe('refresh-123');
  });

  // ── User Metadata ─────────────────────────────────────────────────

  it('should return null when no user is stored', () => {
    expect(service.getUser()).toBeNull();
  });

  it('should store and retrieve user', () => {
    service.setUser(testUser);
    const retrieved = service.getUser();
    expect(retrieved).toEqual(testUser);
  });

  // ── saveSession ───────────────────────────────────────────────────

  it('should save all session data in one call', () => {
    service.saveSession('tok-a', 'ref-b', testUser);

    expect(service.getAccessToken()).toBe('tok-a');
    expect(service.getRefreshToken()).toBe('ref-b');
    expect(service.getUser()?.email).toBe('alice@example.com');
  });

  // ── isLoggedIn ────────────────────────────────────────────────────

  it('should return false when not logged in', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return true after setting an access token', () => {
    service.setAccessToken('some-token');
    expect(service.isLoggedIn()).toBeTrue();
  });

  // ── clear ─────────────────────────────────────────────────────────

  it('should clear all stored data', () => {
    service.saveSession('tok', 'ref', testUser);
    service.clear();

    expect(service.getAccessToken()).toBeNull();
    expect(service.getRefreshToken()).toBeNull();
    expect(service.getUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  // ── Rehydration from localStorage ─────────────────────────────────

  it('should rehydrate access token from localStorage on construction', () => {
    localStorage.setItem('sm_access_token', 'persisted-token');

    // Create a new instance to test constructor rehydration
    const freshService = new TokenStorageService();
    expect(freshService.getAccessToken()).toBe('persisted-token');
  });
});

