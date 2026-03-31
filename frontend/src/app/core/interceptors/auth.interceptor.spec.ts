import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';
import { of, throwError } from 'rxjs';
import { AuthTokens } from '../models/auth.models';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenService: jest.Mocked<TokenService>;
  let authService: jest.Mocked<AuthService>;

  const mockTokens: AuthTokens = {
    accessToken: 'new-access-token',
    refreshToken: 'new-refresh-token',
    tokenType: 'Bearer',
    expiresIn: 900,
  };

  beforeEach(() => {
    const tokenServiceMock: jest.Mocked<Partial<TokenService>> = {
      getAccessToken: jest.fn(),
      getRefreshToken: jest.fn(),
    };

    const authServiceMock: jest.Mocked<Partial<AuthService>> = {
      refresh: jest.fn(),
      clearSession: jest.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: TokenService, useValue: tokenServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService) as jest.Mocked<TokenService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
  });

  afterEach(() => httpMock.verify());

  it('authInterceptor_whenTokenPresent_thenAddsAuthorizationHeader', () => {
    tokenService.getAccessToken.mockReturnValue('my-access-token');

    http.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-access-token');
    req.flush({});
  });

  it('authInterceptor_whenNoToken_thenDoesNotAddAuthorizationHeader', () => {
    tokenService.getAccessToken.mockReturnValue(null);

    http.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('authInterceptor_when401OnProtectedRoute_thenRefreshesTokenAndRetries', () => {
    tokenService.getAccessToken.mockReturnValue('expired-token');
    (authService.refresh as jest.Mock).mockReturnValue(of(mockTokens));

    http.get('/api/protected').subscribe();

    const firstReq = httpMock.expectOne('/api/protected');
    firstReq.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    const retryReq = httpMock.expectOne('/api/protected');
    expect(retryReq.request.headers.get('Authorization')).toBe('Bearer new-access-token');
    retryReq.flush({ data: 'success' });

    expect(authService.refresh).toHaveBeenCalledTimes(1);
  });

  it('authInterceptor_when401OnAuthRoute_thenDoesNotRefresh', () => {
    tokenService.getAccessToken.mockReturnValue('token');

    http.get('/api/auth/login').subscribe({ error: () => {} });

    httpMock
      .expectOne('/api/auth/login')
      .flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.refresh).not.toHaveBeenCalled();
  });

  it('authInterceptor_whenRefreshFails_thenClearsSessionAndPropagatesError', () => {
    tokenService.getAccessToken.mockReturnValue('expired-token');
    (authService.refresh as jest.Mock).mockReturnValue(
      throwError(() => new Error('Refresh failed'))
    );

    let errorReceived = false;
    http.get('/api/protected').subscribe({ error: () => (errorReceived = true) });

    httpMock
      .expectOne('/api/protected')
      .flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.clearSession).toHaveBeenCalled();
    expect(errorReceived).toBe(true);
  });
});
