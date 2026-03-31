import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { guestGuard } from './guest.guard';
import { TokenService } from '../services/token.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('guestGuard', () => {
  let tokenService: jest.Mocked<TokenService>;
  let router: jest.Mocked<Router>;

  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = {} as RouterStateSnapshot;

  beforeEach(() => {
    const tokenServiceMock: jest.Mocked<Partial<TokenService>> = {
      hasTokens: jest.fn(),
    };

    const routerMock: jest.Mocked<Partial<Router>> = {
      createUrlTree: jest.fn().mockReturnValue({ toString: () => '/dashboard' }),
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

  it('guestGuard_whenNoTokens_thenReturnsTrue', () => {
    tokenService.hasTokens.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      guestGuard(dummyRoute, dummyState)
    );

    expect(result).toBe(true);
  });

  it('guestGuard_whenTokensPresent_thenRedirectsToDashboard', () => {
    tokenService.hasTokens.mockReturnValue(true);

    TestBed.runInInjectionContext(() => guestGuard(dummyRoute, dummyState));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
  });
});
