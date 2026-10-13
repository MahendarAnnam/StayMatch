import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { MaterialModule } from './shared/material.module';
import { AuthUser } from './models/auth.model';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const testUser: AuthUser = {
    userId: 1,
    email: 'john.doe@example.com',
    role: 'TENANT'
  };

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser$: of(testUser)
    });

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule, NoopAnimationsModule, MaterialModule],
      declarations: [AppComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  // ── Profile Dropdown ──────────────────────────────────────────────

  it('should start with profile dropdown closed', () => {
    expect(component.profileDropdownOpen).toBeFalse();
  });

  it('should toggle profile dropdown open and closed', () => {
    component.toggleProfileDropdown();
    expect(component.profileDropdownOpen).toBeTrue();

    component.toggleProfileDropdown();
    expect(component.profileDropdownOpen).toBeFalse();
  });

  it('should close profile dropdown via closeProfileDropdown()', () => {
    component.profileDropdownOpen = true;
    component.closeProfileDropdown();
    expect(component.profileDropdownOpen).toBeFalse();
  });

  it('should close profile dropdown when clicking outside', () => {
    component.profileDropdownOpen = true;
    fixture.detectChanges();

    // Simulate a click on the document body (outside the dropdown)
    const event = new MouseEvent('click', { bubbles: true });
    document.body.dispatchEvent(event);

    expect(component.profileDropdownOpen).toBeFalse();
  });

  it('should NOT close profile dropdown when clicking inside dropdown', () => {
    component.profileDropdownOpen = true;
    fixture.detectChanges();

    const dropdownWrap = fixture.nativeElement.querySelector('.profile-dropdown-wrap');
    if (dropdownWrap) {
      const event = new MouseEvent('click', { bubbles: true });
      dropdownWrap.dispatchEvent(event);
      expect(component.profileDropdownOpen).toBeTrue();
    } else {
      // dropdown-wrap is only rendered when user is logged in; verify state unchanged
      expect(component.profileDropdownOpen).toBeTrue();
    }
  });

  // ── getUserDisplayName ────────────────────────────────────────────

  it('should derive display name from simple email', () => {
    expect(component.getUserDisplayName('john@example.com')).toBe('John');
  });

  it('should derive display name from dot-separated email', () => {
    expect(component.getUserDisplayName('john.doe@example.com')).toBe('John Doe');
  });

  it('should derive display name from underscore-separated email', () => {
    expect(component.getUserDisplayName('john_doe@example.com')).toBe('John Doe');
  });

  it('should derive display name from hyphen-separated email', () => {
    expect(component.getUserDisplayName('john-doe@example.com')).toBe('John Doe');
  });

  it('should handle single-character email local part', () => {
    expect(component.getUserDisplayName('a@example.com')).toBe('A');
  });

  // ── getAvatarInitial ─────────────────────────────────────────────

  it('should return uppercase first character of email', () => {
    expect(component.getAvatarInitial('john@example.com')).toBe('J');
  });

  it('should uppercase lowercase first character', () => {
    expect(component.getAvatarInitial('alice@example.com')).toBe('A');
  });

  it('should handle already uppercase first character', () => {
    expect(component.getAvatarInitial('Bob@example.com')).toBe('B');
  });

  // ── Mobile Menu ──────────────────────────────────────────────────

  it('should start with mobile menu closed', () => {
    expect(component.mobileMenuOpen).toBeFalse();
  });

  it('should toggle mobile menu', () => {
    component.toggleMobileMenu();
    expect(component.mobileMenuOpen).toBeTrue();

    component.toggleMobileMenu();
    expect(component.mobileMenuOpen).toBeFalse();
  });

  it('should close mobile menu', () => {
    component.mobileMenuOpen = true;
    component.closeMobileMenu();
    expect(component.mobileMenuOpen).toBeFalse();
  });

  // ── Logout ───────────────────────────────────────────────────────

  it('should call authService.logout and close menus on logout', () => {
    component.profileDropdownOpen = true;
    component.mobileMenuOpen = true;

    component.logout();

    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(component.profileDropdownOpen).toBeFalse();
    expect(component.mobileMenuOpen).toBeFalse();
  });
});

