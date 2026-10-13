import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';
import { environment } from '../../environments/environment';

describe('AuthInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockTokenStorage: jasmine.SpyObj<TokenStorageService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const apiUrl = `${environment.apiBaseUrl}/pg`;

  beforeEach(() => {
    mockTokenStorage = jasmine.createSpyObj('TokenStorageService', [
      'getAccessToken',
      'getRefreshToken',
      'setAccessToken'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['refreshToken', 'logout']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: TokenStorageService, useValue: mockTokenStorage },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header when access token exists', () => {
    mockTokenStorage.getAccessToken.and.returnValue('my-token');

    httpClient.get(apiUrl).subscribe();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-token');
    req.flush({});
  });

  it('should NOT add Authorization header when no access token', () => {
    mockTokenStorage.getAccessToken.and.returnValue(null);

    httpClient.get(apiUrl).subscribe();

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should NOT intercept external URLs', () => {
    const externalUrl = 'https://nominatim.openstreetmap.org/reverse';
    mockTokenStorage.getAccessToken.and.returnValue('my-token');

    httpClient.get(externalUrl).subscribe();

    const req = httpMock.expectOne(externalUrl);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should NOT intercept refresh endpoint', () => {
    const refreshUrl = `${environment.apiBaseUrl}/auth/refresh`;
    mockTokenStorage.getAccessToken.and.returnValue('my-token');

    httpClient.post(refreshUrl, {}).subscribe();

    const req = httpMock.expectOne(refreshUrl);
    // The interceptor should skip this URL entirely
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});

