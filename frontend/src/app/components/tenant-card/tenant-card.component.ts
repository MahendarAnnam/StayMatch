import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { TenantPost } from '../../models/tenant.model';

@Component({
  selector: 'app-tenant-card',
  templateUrl: './tenant-card.component.html',
  styleUrls: ['./tenant-card.component.css']
})
export class TenantCardComponent {
  @Input() tenant!: TenantPost;

  constructor(private readonly router: Router) {}

  get availableSpots(): number {
    return this.tenant.availableSpots
      ?? (this.tenant.totalCapacity - this.tenant.currentOccupancy);
  }

  /** Occupancy as a 0–100 percentage for the progress bar. */
  get occupancyPercent(): number {
    if (!this.tenant || this.tenant.totalCapacity === 0) return 0;
    return Math.round((this.tenant.currentOccupancy / this.tenant.totalCapacity) * 100);
  }

  /** CSS class for the occupancy bar colour based on fill level. */
  get occupancyColor(): string {
    const pct = this.occupancyPercent;
    if (pct >= 100) return 'occupancy-fill--full';
    if (pct >= 75)  return 'occupancy-fill--high';
    if (pct >= 50)  return 'occupancy-fill--mid';
    return 'occupancy-fill--low';
  }

  get prefChips(): string[] {
    const p = this.tenant.preference;
    if (!p) return [];
    const chips: string[] = [];
    if (p.foodPreference) chips.push(p.foodPreference);
    if (p.smokingPreference) chips.push(p.smokingPreference);
    if (p.preferredGender) chips.push(p.preferredGender);
    return chips;
  }

  openDetail(): void {
    this.router.navigate(['/roommate', this.tenant.id]);
  }
}

