import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { AuthTokens, LoginRequest, RegisterRequest, User } from '../models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenService: jest.Mocked<TokenService>;
  let router: jest.Mocked<Router>;

  const mockTokens: AuthTokens = {
    accessToken: 'access-token',
    refreshToken: 'refresh-token',
    tokenType: 'Bearer',
    expiresIn: 900,
  };

  const mockUser: User = {
    id: 'user-1',
    email: 'test@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['USER'],
  };

  beforeEach(() => {
    const tokenServiceMock: jest.Mocked<TokenService> = {
      saveTokens: jest.fn(),
      getAccessToken: jest.fn(),
      getRefreshToken: jest.fn(),
      clearTokens: jest.fn(),
      hasTokens: jest.fn(),
    };

    const routerMock: jest.Mocked<Partial<Router>> = {
      navigate: jest.fn().mockResolvedValue(true),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: TokenService, useValue: tokenServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService) as jest.Mocked<TokenService>;
    router = TestBed.inject(Router) as jest.Mocked<Router>;
  });

  afterEach(() => httpMock.verify());

  // ── Initial state ──────────────────────────────────────────────────────────

  describe('Initial state', () => {
    it('currentUser_whenInitialized_thenIsNull', () => {
      expect(service.currentUser()).toBeNull();
    });

    it('isAuthenticated_whenInitialized_thenIsFalse', () => {
      expect(service.isAuthenticated()).toBe(false);
    });
  });

  // ── login() ────────────────────────────────────────────────────────────────

  describe('login()', () => {
    const request: LoginRequest = { email: 'test@example.com', password: 'password123' };

    it('login_whenValidCredentials_thenPostsToLoginEndpoint', () => {
      service.login(request).subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockTokens);
    });

    it('login_whenSuccess_thenSavesTokens', () => {
      service.login(request).subscribe();

      httpMock.expectOne('/api/auth/login').flush(mockTokens);

      expect(tokenService.saveTokens).toHaveBeenCalledWith(mockTokens);
    });

    it('login_whenSuccess_thenReturnsTokens', (done) => {
      service.login(request).subscribe((tokens) => {
        expect(tokens).toEqual(mockTokens);
        done();
      });

      httpMock.expectOne('/api/auth/login').flush(mockTokens);
    });
  });

  // ── register() ────────────────────────────────────────────────────────────

  describe('register()', () => {
    const request: RegisterRequest = {
      email: 'new@example.com',
      password: 'password123',
      firstName: 'Jane',
      lastName: 'Doe',
    };

    it('register_whenCalled_thenPostsToRegisterEndpoint', () => {
      service.register(request).subscribe();

      const req = httpMock.expectOne('/api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(null);
    });
  });

  // ── logout() ──────────────────────────────────────────────────────────────

  describe('logout()', () => {
    it('logout_whenCalled_thenPostsRefreshTokenToLogoutEndpoint', () => {
      tokenService.getRefreshToken.mockReturnValue('refresh-token');

      service.logout().subscribe();

      const req = httpMock.expectOne('/api/auth/logout');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'refresh-token' });
      req.flush(null);
    });

    it('logout_whenSuccess_thenClearsTokensAndUserAndNavigatesToLogin', () => {
      tokenService.getRefreshToken.mockReturnValue('refresh-token');
      service.setCurrentUser(mockUser);

      service.logout().subscribe();
      httpMock.expectOne('/api/auth/logout').flush(null);

      expect(tokenService.clearTokens).toHaveBeenCalled();
      expect(service.currentUser()).toBeNull();
      expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
    });
  });

  // ── refresh() ─────────────────────────────────────────────────────────────

  describe('refresh()', () => {
    it('refresh_whenCalled_thenPostsToRefreshEndpoint', () => {
      tokenService.getRefreshToken.mockReturnValue('old-refresh-token');

      service.refresh().subscribe();

      const req = httpMock.expectOne('/api/auth/refresh');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
      req.flush(mockTokens);
    });

    it('refresh_whenSuccess_thenSavesNewTokens', () => {
      tokenService.getRefreshToken.mockReturnValue('old-refresh-token');

      service.refresh().subscribe();
      httpMock.expectOne('/api/auth/refresh').flush(mockTokens);

      expect(tokenService.saveTokens).toHaveBeenCalledWith(mockTokens);
    });
  });

  // ── loadCurrentUser() ────────────────────────────────────────────────────

  describe('loadCurrentUser()', () => {
    it('loadCurrentUser_whenSuccess_thenSetsCurrentUserSignal', (done) => {
      service.loadCurrentUser().subscribe(() => {
        expect(service.currentUser()).toEqual(mockUser);
        expect(service.isAuthenticated()).toBe(true);
        done();
      });

      httpMock.expectOne('/api/users/me').flush(mockUser);
    });
  });

  // ── setCurrentUser() ──────────────────────────────────────────────────────

  describe('setCurrentUser()', () => {
    it('setCurrentUser_whenCalled_thenUpdatesCurrentUserSignal', () => {
      service.setCurrentUser(mockUser);

      expect(service.currentUser()).toEqual(mockUser);
      expect(service.isAuthenticated()).toBe(true);
    });
  });

  // ── clearSession() ────────────────────────────────────────────────────────

  describe('clearSession()', () => {
    it('clearSession_whenCalled_thenClearsTokensAndSetsUserToNull', () => {
      service.setCurrentUser(mockUser);

      service.clearSession();

      expect(tokenService.clearTokens).toHaveBeenCalled();
      expect(service.currentUser()).toBeNull();
      expect(service.isAuthenticated()).toBe(false);
    });
  });

  // ── forgotPassword() ─────────────────────────────────────────────────────

  describe('forgotPassword()', () => {
    it('forgotPassword_whenCalled_thenPostsToForgotPasswordEndpoint', () => {
      service.forgotPassword({ email: 'test@example.com' }).subscribe();

      const req = httpMock.expectOne('/api/auth/forgot-password');
      expect(req.request.method).toBe('POST');
      req.flush(null);
    });
  });

  // ── resetPassword() ──────────────────────────────────────────────────────

  describe('resetPassword()', () => {
    it('resetPassword_whenCalled_thenPostsToResetPasswordEndpoint', () => {
      service.resetPassword({ token: 'reset-token', newPassword: 'newpass123' }).subscribe();

      const req = httpMock.expectOne('/api/auth/reset-password');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ token: 'reset-token', newPassword: 'newpass123' });
      req.flush(null);
    });
  });
});
