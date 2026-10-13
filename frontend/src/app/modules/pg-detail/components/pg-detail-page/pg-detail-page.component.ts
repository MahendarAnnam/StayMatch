import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { PgListing, PgImage } from '../../../../models/pg.model';
import { PgService } from '../../../../services/pg.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-pg-detail-page',
  templateUrl: './pg-detail-page.component.html',
  styleUrls: ['./pg-detail-page.component.css']
})
export class PgDetailPageComponent implements OnInit, OnDestroy {

  pg: PgListing | null = null;
  loading = true;
  errorMessage: string | null = null;
  deleting = false;
  deleteConfirm = false;

  /** Sorted images for the gallery. */
  galleryImages: PgImage[] = [];
  /** Currently displayed image index. */
  activeImageIndex = 0;

  private sub: Subscription | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly pgService: PgService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage = 'Invalid PG ID.';
      this.loading = false;
      return;
    }
    this.fetchPg(id);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  // ── Gallery controls ────────────────────────────────────────────────

  get activeImage(): PgImage | null {
    return this.galleryImages[this.activeImageIndex] ?? null;
  }

  prevImage(): void {
    if (this.activeImageIndex > 0) {
      this.activeImageIndex--;
    }
  }

  nextImage(): void {
    if (this.activeImageIndex < this.galleryImages.length - 1) {
      this.activeImageIndex++;
    }
  }

  selectImage(index: number): void {
    this.activeImageIndex = index;
  }

  // ── Navigation ──────────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/pg-search']);
  }

  retry(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchPg(id);
    }
  }

  // ── Edit / Delete ────────────────────────────────────────────────────

  get canEditOrDelete(): boolean {
    const user = this.authService.getCurrentUser();
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    if (user.role === 'PG_OWNER' && this.pg?.owner?.email === user.email) return true;
    return false;
  }

  editPg(): void {
    if (this.pg) {
      this.router.navigate(['/pg', this.pg.id, 'edit']);
    }
  }

  confirmDelete(): void {
    this.deleteConfirm = true;
  }

  cancelDelete(): void {
    this.deleteConfirm = false;
  }

  deletePg(): void {
    if (!this.pg) return;
    this.deleting = true;
    this.pgService.deletePg(this.pg.id).subscribe({
      next: () => {
        this.router.navigate(['/pg-search']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to delete PG.';
        this.deleting = false;
        this.deleteConfirm = false;
      }
    });
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  /** Returns the lowest rent from all sharing options. */
  get cheapestRent(): number | null {
    if (!this.pg || !this.pg.sharingOptions || this.pg.sharingOptions.length === 0) {
      return null;
    }
    return Math.min(...this.pg.sharingOptions.map((o) => o.rent));
  }

  /** Price display text based on priceType. */
  get priceDisplayText(): string {
    if (!this.pg) return '';
    switch (this.pg.priceType) {
      case 'RANGE':
        return `₹${this.pg.minPrice?.toLocaleString('en-IN') ?? '?'}–₹${this.pg.maxPrice?.toLocaleString('en-IN') ?? '?'}`;
      case 'HIDDEN':
        return 'Contact for price';
      case 'EXACT':
      default:
        return this.cheapestRent !== null
          ? `₹${this.cheapestRent.toLocaleString('en-IN')}`
          : '';
    }
  }

  /** Label for the price stat block. */
  get priceStatLabel(): string {
    if (!this.pg) return 'Starting from';
    switch (this.pg.priceType) {
      case 'RANGE':  return 'Price range';
      case 'HIDDEN': return 'Pricing';
      default:       return 'Starting from';
    }
  }

  /** Total available beds across all sharing types. */
  get totalAvailability(): number {
    if (!this.pg || !this.pg.sharingOptions) return 0;
    return this.pg.sharingOptions.reduce((sum, o) => sum + o.availabilityCount, 0);
  }

  // ── Food helpers ──────────────────────────────────────────────────────

  /** Ordered days of the week for the menu table. */
  readonly daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  /** Short day labels for table headers. */
  readonly shortDays: Record<string, string> = {
    MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed',
    THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun'
  };

  /** Human-readable food type label. */
  get foodTypeLabel(): string | null {
    if (!this.pg?.foodType) return null;
    switch (this.pg.foodType) {
      case 'VEG':     return 'Pure Veg';
      case 'NON_VEG': return 'Non-Veg';
      case 'BOTH':    return 'Veg & Non-Veg';
      default:        return null;
    }
  }

  /** CSS class for the food type badge. */
  get foodBadgeClass(): string {
    switch (this.pg?.foodType) {
      case 'VEG':     return 'food-badge--veg';
      case 'NON_VEG': return 'food-badge--nonveg';
      case 'BOTH':    return 'food-badge--both';
      default:        return '';
    }
  }

  /** Returns menu items for a given day. */
  getMenuForDay(day: string): string[] {
    return this.pg?.weeklyMenu?.[day] ?? [];
  }

  /** Whether the weekly menu has any data. */
  get hasWeeklyMenu(): boolean {
    return !!this.pg?.weeklyMenu && Object.keys(this.pg.weeklyMenu).length > 0;
  }

  /** Check if a day is a non-veg day. */
  isNonVegDay(day: string): boolean {
    return this.pg?.nonVegDays?.includes(day) ?? false;
  }

  /** Check if a day is an egg day. */
  isEggDay(day: string): boolean {
    return this.pg?.eggDays?.includes(day) ?? false;
  }

  /** Check if a menu item looks "special" (highlight-worthy). */
  isSpecialMeal(item: string): boolean {
    const specials = ['biryani', 'mutton', 'fish', 'chicken pulao', 'chole bhature', 'misal pav', 'vada pav'];
    return specials.some(s => item.toLowerCase().includes(s));
  }

  // ── Private ─────────────────────────────────────────────────────────

  private fetchPg(id: string): void {
    this.loading = true;
    this.errorMessage = null;

    this.sub = this.pgService.getPgById(id).subscribe({
      next: (pg) => {
        this.pg = pg;
        this.galleryImages = [...(pg.images ?? [])].sort(
          (a, b) => a.displayOrder - b.displayOrder
        );
        this.activeImageIndex = 0;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage =
          err.status === 404
            ? 'PG listing not found.'
            : (err.error?.message ?? 'Failed to load PG details.');
        this.loading = false;
      }
    });
  }
}

