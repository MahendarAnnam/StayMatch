import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { PgListing } from '../../models/pg.model';

@Component({
  selector: 'app-pg-card',
  templateUrl: './pg-card.component.html',
  styleUrls: ['./pg-card.component.css']
})
export class PgCardComponent {
  @Input() pg!: PgListing;

  /** Max facilities shown before "+N more" indicator. */
  readonly maxFacilities = 3;

  /** Max sharing options shown before "+N more" indicator. */
  readonly maxSharing = 2;

  constructor(private readonly router: Router) {}

  /** Lowest rent across all sharing options (for the headline price). */
  get cheapestRent(): number | null {
    if (!this.pg.sharingOptions || this.pg.sharingOptions.length === 0) {
      return null;
    }
    return Math.min(...this.pg.sharingOptions.map((o) => o.rent));
  }

  /** First image URL (if any) for the card thumbnail. */
  get thumbnail(): string | null {
    if (!this.pg.images || this.pg.images.length === 0) {
      return null;
    }
    const sorted = [...this.pg.images].sort((a, b) => a.displayOrder - b.displayOrder);
    return sorted[0].imageUrl;
  }

  /** Visible sharing options (capped). */
  get visibleSharing() {
    return (this.pg.sharingOptions ?? []).slice(0, this.maxSharing);
  }

  /** Number of hidden sharing options. */
  get hiddenSharingCount(): number {
    return Math.max(0, (this.pg.sharingOptions?.length ?? 0) - this.maxSharing);
  }

  /** Visible facilities (capped). */
  get visibleFacilities(): string[] {
    return (this.pg.facilities ?? []).slice(0, this.maxFacilities);
  }

  /** Number of hidden facilities. */
  get hiddenFacilityCount(): number {
    return Math.max(0, (this.pg.facilities?.length ?? 0) - this.maxFacilities);
  }

  openDetails(): void {
    this.router.navigate(['/pg', this.pg.id]);
  }

  /** Human-readable food type label. */
  get foodTypeLabel(): string | null {
    if (!this.pg.foodType) return null;
    switch (this.pg.foodType) {
      case 'VEG':     return 'Pure Veg';
      case 'NON_VEG': return 'Non-Veg';
      case 'BOTH':    return 'Veg & Non-Veg';
      default:        return null;
    }
  }

  /** CSS class for the food type badge. */
  get foodTypeBadgeClass(): string {
    switch (this.pg.foodType) {
      case 'VEG':     return 'food-badge--veg';
      case 'NON_VEG': return 'food-badge--nonveg';
      case 'BOTH':    return 'food-badge--both';
      default:        return '';
    }
  }

  /** Brief non-veg days summary for the card (e.g. "Non-veg: Wed, Sun"). */
  get nonVegDaysSummary(): string | null {
    if (!this.pg.nonVegDays || this.pg.nonVegDays.length === 0) return null;
    const shortNames: Record<string, string> = {
      MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed',
      THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun'
    };
    const days = this.pg.nonVegDays.map(d => shortNames[d] || d).join(', ');
    return `Non-veg: ${days}`;
  }

  /** Price display text based on priceType. */
  get priceDisplayText(): string {
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

  /** Whether the price badge should show. */
  get showPriceBadge(): boolean {
    if (this.pg.priceType === 'HIDDEN') return true;
    if (this.pg.priceType === 'RANGE') return !!(this.pg.minPrice && this.pg.maxPrice);
    return this.cheapestRent !== null;
  }
}

