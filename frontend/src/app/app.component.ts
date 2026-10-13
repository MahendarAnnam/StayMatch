import { Component, ElementRef, HostListener } from '@angular/core';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  mobileMenuOpen = false;
  profileDropdownOpen = false;

  constructor(
    readonly authService: AuthService,
    private readonly elRef: ElementRef
  ) {}

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  toggleProfileDropdown(): void {
    this.profileDropdownOpen = !this.profileDropdownOpen;
  }

  closeProfileDropdown(): void {
    this.profileDropdownOpen = false;
  }

  /** Close profile dropdown when clicking outside of it. */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.profileDropdownOpen) return;
    const target = event.target as HTMLElement;
    const dropdown = this.elRef.nativeElement.querySelector('.profile-dropdown-wrap');
    if (dropdown && !dropdown.contains(target)) {
      this.profileDropdownOpen = false;
    }
  }

  logout(): void {
    this.authService.logout();
    this.closeProfileDropdown();
    this.closeMobileMenu();
  }

  /** Derive a display name from email (part before @, title-cased). */
  getUserDisplayName(email: string): string {
    const local = email.split('@')[0] ?? '';
    return local
      .replace(/[._-]/g, ' ')
      .replace(/\b\w/g, (c) => c.toUpperCase());
  }

  /** Get the first letter of the email as avatar initial. */
  getAvatarInitial(email: string): string {
    return email.charAt(0).toUpperCase();
  }
}
