import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';
import { AuthTokens } from '../models/auth.models';

describe('TokenService', () => {
  let service: TokenService;

  const mockTokens: AuthTokens = {
    accessToken: 'mock-access-token',
    refreshToken: 'mock-refresh-token',
    tokenType: 'Bearer',
    expiresIn: 900,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  describe('saveTokens()', () => {
    it('saveTokens_whenCalled_thenPersistsBothTokensToLocalStorage', () => {
      service.saveTokens(mockTokens);

      expect(localStorage.getItem('catalyst_access_token')).toBe('mock-access-token');
      expect(localStorage.getItem('catalyst_refresh_token')).toBe('mock-refresh-token');
    });
  });

  describe('getAccessToken()', () => {
    it('getAccessToken_whenTokenSaved_thenReturnsAccessToken', () => {
      service.saveTokens(mockTokens);

      expect(service.getAccessToken()).toBe('mock-access-token');
    });

    it('getAccessToken_whenNoTokenSaved_thenReturnsNull', () => {
      expect(service.getAccessToken()).toBeNull();
    });
  });

  describe('getRefreshToken()', () => {
    it('getRefreshToken_whenTokenSaved_thenReturnsRefreshToken', () => {
      service.saveTokens(mockTokens);

      expect(service.getRefreshToken()).toBe('mock-refresh-token');
    });

    it('getRefreshToken_whenNoTokenSaved_thenReturnsNull', () => {
      expect(service.getRefreshToken()).toBeNull();
    });
  });

  describe('clearTokens()', () => {
    it('clearTokens_whenCalled_thenRemovesBothTokensFromLocalStorage', () => {
      service.saveTokens(mockTokens);
      service.clearTokens();

      expect(service.getAccessToken()).toBeNull();
      expect(service.getRefreshToken()).toBeNull();
    });
  });

  describe('hasTokens()', () => {
    it('hasTokens_whenBothTokensPresent_thenReturnsTrue', () => {
      service.saveTokens(mockTokens);

      expect(service.hasTokens()).toBe(true);
    });

    it('hasTokens_whenNoTokensPresent_thenReturnsFalse', () => {
      expect(service.hasTokens()).toBe(false);
    });

    it('hasTokens_whenOnlyAccessTokenPresent_thenReturnsFalse', () => {
      localStorage.setItem('catalyst_access_token', 'only-access');

      expect(service.hasTokens()).toBe(false);
    });

    it('hasTokens_whenOnlyRefreshTokenPresent_thenReturnsFalse', () => {
      localStorage.setItem('catalyst_refresh_token', 'only-refresh');

      expect(service.hasTokens()).toBe(false);
    });
  });
});
