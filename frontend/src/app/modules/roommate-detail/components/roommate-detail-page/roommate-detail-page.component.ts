import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TenantPost } from '../../../../models/tenant.model';
import { TenantService } from '../../../../services/tenant.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-roommate-detail-page',
  templateUrl: './roommate-detail-page.component.html',
  styleUrls: ['./roommate-detail-page.component.css']
})
export class RoommateDetailPageComponent implements OnInit, OnDestroy {

  post: TenantPost | null = null;
  loading = true;
  errorMessage: string | null = null;
  deleting = false;
  deleteConfirm = false;

  private sub: Subscription | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly tenantService: TenantService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage = 'Invalid roommate listing ID.';
      this.loading = false;
      return;
    }
    this.fetchPost(Number(id));
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  // ── Navigation ──────────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/tenant-matching']);
  }

  retry(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchPost(Number(id));
    }
  }

  // ── Computed helpers ────────────────────────────────────────────────

  get availableSpots(): number {
    if (!this.post) return 0;
    return this.post.availableSpots
      ?? (this.post.totalCapacity - this.post.currentOccupancy);
  }

  get budgetDisplay(): string {
    if (!this.post) return '';
    return `₹${this.post.minBudget.toLocaleString('en-IN')} – ₹${this.post.maxBudget.toLocaleString('en-IN')}`;
  }

  get occupancyPercent(): number {
    if (!this.post || this.post.totalCapacity === 0) return 0;
    return Math.round((this.post.currentOccupancy / this.post.totalCapacity) * 100);
  }

  get foodPreferenceLabel(): string | null {
    const fp = this.post?.preference?.foodPreference;
    if (!fp) return null;
    switch (fp) {
      case 'VEG':     return 'Pure Veg';
      case 'NON_VEG': return 'Non-Veg';
      case 'BOTH':    return 'Veg & Non-Veg';
      default:        return fp;
    }
  }

  get foodBadgeClass(): string {
    switch (this.post?.preference?.foodPreference) {
      case 'VEG':     return 'pref-badge--veg';
      case 'NON_VEG': return 'pref-badge--nonveg';
      case 'BOTH':    return 'pref-badge--both';
      default:        return '';
    }
  }

  get smokingLabel(): string | null {
    const sp = this.post?.preference?.smokingPreference;
    if (!sp) return null;
    switch (sp) {
      case 'NO_SMOKING':      return 'No Smoking';
      case 'SMOKING_ALLOWED': return 'Smoking Allowed';
      default:                return sp;
    }
  }

  get smokingBadgeClass(): string {
    switch (this.post?.preference?.smokingPreference) {
      case 'NO_SMOKING':      return 'pref-badge--no-smoking';
      case 'SMOKING_ALLOWED': return 'pref-badge--smoking';
      default:                return '';
    }
  }

  get genderLabel(): string | null {
    const gp = this.post?.preference?.preferredGender;
    if (!gp) return null;
    switch (gp) {
      case 'MALE':   return 'Male Only';
      case 'FEMALE': return 'Female Only';
      case 'ANY':    return 'Any Gender';
      default:       return gp;
    }
  }

  get genderBadgeClass(): string {
    switch (this.post?.preference?.preferredGender) {
      case 'MALE':   return 'pref-badge--male';
      case 'FEMALE': return 'pref-badge--female';
      case 'ANY':    return 'pref-badge--any';
      default:       return '';
    }
  }

  /** Format move-in date for display. */
  get moveInDateDisplay(): string | null {
    if (!this.post?.moveInDate) return null;
    try {
      const d = new Date(this.post.moveInDate);
      return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch {
      return this.post.moveInDate;
    }
  }

  /** Format created date for display. */
  get postedDateDisplay(): string | null {
    if (!this.post?.createdAt) return null;
    try {
      const d = new Date(this.post.createdAt);
      return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch {
      return this.post.createdAt;
    }
  }

  // ── CTA helpers ────────────────────────────────────────────────────

  get phoneNumber(): string | null {
    return this.post?.user?.phoneNumber ?? null;
  }

  get email(): string | null {
    return this.post?.user?.email ?? null;
  }

  get callLink(): string {
    return `tel:${this.phoneNumber}`;
  }

  get whatsappLink(): string {
    const phone = (this.phoneNumber ?? '').replace(/[^0-9]/g, '');
    const msg = encodeURIComponent(
      `Hi, I'm interested in your roommate listing "${this.post?.title ?? ''}" on StayMatch.`
    );
    return `https://wa.me/${phone}?text=${msg}`;
  }

  get emailLink(): string {
    const subject = encodeURIComponent(`Interested in: ${this.post?.title ?? 'Your Roommate Listing'}`);
    const body = encodeURIComponent(
      `Hi ${this.post?.user?.fullName ?? ''},\n\nI saw your roommate listing on StayMatch and I'm interested. Could we discuss the details?\n\nThanks!`
    );
    return `mailto:${this.email}?subject=${subject}&body=${body}`;
  }

  // ── Edit / Delete ────────────────────────────────────────────────────

  get canEditOrDelete(): boolean {
    const user = this.authService.getCurrentUser();
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    if (this.post?.user?.email === user.email) return true;
    return false;
  }

  confirmDelete(): void {
    this.deleteConfirm = true;
  }

  cancelDelete(): void {
    this.deleteConfirm = false;
  }

  deletePost(): void {
    if (!this.post) return;
    this.deleting = true;
    this.tenantService.deleteTenantPost(this.post.id).subscribe({
      next: () => {
        this.router.navigate(['/tenant-matching']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to delete listing.';
        this.deleting = false;
        this.deleteConfirm = false;
      }
    });
  }

  // ── Private ─────────────────────────────────────────────────────────

  private fetchPost(id: number): void {
    this.loading = true;
    this.errorMessage = null;

    this.sub = this.tenantService.getTenantPostById(id).subscribe({
      next: (post) => {
        this.post = post;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage =
          err.status === 404
            ? 'Roommate listing not found.'
            : (err.error?.message ?? 'Failed to load roommate details.');
        this.loading = false;
      }
    });
  }
}

