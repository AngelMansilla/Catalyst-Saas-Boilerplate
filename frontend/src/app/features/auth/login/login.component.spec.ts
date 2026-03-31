import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { AuthTokens, User } from '../../../core/models/auth.models';
import { provideRouter } from '@angular/router';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let authService: jest.Mocked<AuthService>;
  let snackBar: jest.Mocked<MatSnackBar>;
  let router: Router;

  const mockTokens: AuthTokens = {
    accessToken: 'access',
    refreshToken: 'refresh',
    tokenType: 'Bearer',
    expiresIn: 900,
  };

  const mockUser: User = {
    id: '1',
    email: 'test@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['USER'],
  };

  beforeEach(async () => {
    const authServiceMock: jest.Mocked<Partial<AuthService>> = {
      login: jest.fn(),
      loadCurrentUser: jest.fn(),
    };

    const snackBarMock: jest.Mocked<Partial<MatSnackBar>> = {
      open: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        provideRouter([]),
        provideAnimations(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: MatSnackBar, useValue: snackBarMock },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    snackBar = TestBed.inject(MatSnackBar) as jest.Mocked<MatSnackBar>;
    router = TestBed.inject(Router);
  });

  // ── Form validation ────────────────────────────────────────────────────────

  describe('Form validation', () => {
    it('form_whenInitialized_thenIsInvalid', () => {
      expect(component.form.invalid).toBe(true);
    });

    it('form_whenValidEmailAndPassword_thenIsValid', () => {
      component.form.setValue({ email: 'test@example.com', password: 'password123' });

      expect(component.form.valid).toBe(true);
    });

    it('form_whenInvalidEmail_thenEmailControlHasEmailError', () => {
      component.form.get('email')!.setValue('not-an-email');

      expect(component.form.get('email')!.hasError('email')).toBe(true);
    });

    it('form_whenPasswordTooShort_thenPasswordControlHasMinLengthError', () => {
      component.form.get('password')!.setValue('short');

      expect(component.form.get('password')!.hasError('minlength')).toBe(true);
    });
  });

  // ── onSubmit() ────────────────────────────────────────────────────────────

  describe('onSubmit()', () => {
    it('onSubmit_whenFormInvalid_thenDoesNotCallAuthService', () => {
      component.onSubmit();

      expect(authService.login).not.toHaveBeenCalled();
    });

    it('onSubmit_whenValidForm_thenCallsLoginWithFormValues', () => {
      (authService.login as jest.Mock).mockReturnValue(of(mockTokens));
      (authService.loadCurrentUser as jest.Mock).mockReturnValue(of(mockUser));
      component.form.setValue({ email: 'test@example.com', password: 'password123' });

      component.onSubmit();

      expect(authService.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });

    it('onSubmit_whenLoginSuccess_thenNavigatesToDashboard', () => {
      (authService.login as jest.Mock).mockReturnValue(of(mockTokens));
      (authService.loadCurrentUser as jest.Mock).mockReturnValue(of(mockUser));
      component.form.setValue({ email: 'test@example.com', password: 'password123' });
      const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);

      component.onSubmit();

      expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
    });

    it('onSubmit_whenLogin401_thenSetsLoadingToFalseAndDoesNotNavigate', () => {
      (authService.login as jest.Mock).mockReturnValue(throwError(() => ({ status: 401 })));
      component.form.setValue({ email: 'test@example.com', password: 'wrongpassword' });
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.onSubmit();

      expect(component.loading()).toBe(false);
      expect(navigateSpy).not.toHaveBeenCalled();
    });

    it('onSubmit_whenLoginServerError_thenSetsLoadingToFalseAndDoesNotNavigate', () => {
      (authService.login as jest.Mock).mockReturnValue(throwError(() => ({ status: 500 })));
      component.form.setValue({ email: 'test@example.com', password: 'password123' });
      const navigateSpy = jest.spyOn(router, 'navigate');

      component.onSubmit();

      expect(component.loading()).toBe(false);
      expect(navigateSpy).not.toHaveBeenCalled();
    });

    it('onSubmit_whenLoginError_thenSetsLoadingToFalse', () => {
      (authService.login as jest.Mock).mockReturnValue(throwError(() => ({ status: 500 })));
      component.form.setValue({ email: 'test@example.com', password: 'password123' });

      component.onSubmit();

      expect(component.loading()).toBe(false);
    });
  });

  // ── Signals ───────────────────────────────────────────────────────────────

  describe('hidePassword signal', () => {
    it('hidePassword_whenInitialized_thenIsTrue', () => {
      expect(component.hidePassword()).toBe(true);
    });

    it('hidePassword_whenToggled_thenChangesValue', () => {
      component.hidePassword.set(false);

      expect(component.hidePassword()).toBe(false);
    });
  });
});
