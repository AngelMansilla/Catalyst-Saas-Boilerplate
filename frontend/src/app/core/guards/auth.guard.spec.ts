import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { TokenService } from '../services/token.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('authGuard', () => {
  let tokenService: jest.Mocked<TokenService>;
  let router: jest.Mocked<Router>;

  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  beforeEach(() => {
    const tokenServiceMock: jest.Mocked<Partial<TokenService>> = {
      hasTokens: jest.fn(),
    };

    const routerMock: jest.Mocked<Partial<Router>> = {
      createUrlTree: jest.fn().mockReturnValue({ toString: () => '/auth/login' }),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: TokenService, useValue: tokenServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    tokenService = TestBed.inject(TokenService) as jest.Mocked<TokenService>;
    router = TestBed.inject(Router) as jest.Mocked<Router>;
  });

  it('authGuard_whenTokensPresent_thenReturnsTrue', () => {
    tokenService.hasTokens.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(dummyRoute, dummyState)
    );

    expect(result).toBe(true);
  });

  it('authGuard_whenNoTokens_thenRedirectsToLogin', () => {
    tokenService.hasTokens.mockReturnValue(false);

    TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/login']);
  });
});
