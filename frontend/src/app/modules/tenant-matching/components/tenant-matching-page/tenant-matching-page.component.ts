import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { TenantPost, TenantSearchFilters } from '../../../../models/tenant.model';
import { TenantService } from '../../../../services/tenant.service';
import { FilterField, SelectOption } from '../../../../components/filters-panel/filters-panel.component';
import { ResolvedLocation } from '../../../../models/location.model';

const FOOD_OPTIONS = ['VEG', 'NON_VEG', 'BOTH'];
const SMOKING_OPTIONS = ['NO_SMOKING', 'SMOKING_ALLOWED'];
const GENDER_OPTIONS = ['MALE', 'FEMALE', 'ANY'];

const ROOMMATE_SORT_OPTIONS: SelectOption[] = [
  { label: 'Nearest',               value: 'distance,asc' },
  { label: 'Recently Added',        value: 'createdAt,desc' },
  { label: 'Rent: Low → High',     value: 'price,asc' },
  { label: 'Rent: High → Low',     value: 'price,desc' }
];

@Component({
  selector: 'app-tenant-matching-page',
  templateUrl: './tenant-matching-page.component.html',
  styleUrls: ['./tenant-matching-page.component.css']
})
export class TenantMatchingPageComponent implements OnInit, OnDestroy {

  /** Default search radius in km when location is selected. */
  private static readonly DEFAULT_RADIUS_KM = 25;

  // ── Template-bound state ────────────────────────────────────────────
  @ViewChild('resultsPanel') private resultsPanel!: ElementRef<HTMLElement>;

  tenantPosts: TenantPost[] = [];
  loading = false;
  errorMessage: string | null = null;
  filtersOpen = false;

  /** True once the user has selected a location (mandatory before search). */
  locationReady = false;

  /** Location resolved from picker (GPS or city). */
  selectedLocation: ResolvedLocation | null = null;

  /** Filter panel configuration (location handled separately by picker). */
  filterFields: FilterField[] = [
    { key: 'sortBy',            label: 'Sort By',             type: 'select', options: ROOMMATE_SORT_OPTIONS },
    { key: 'budget',            label: 'Budget (₹)',          type: 'number', placeholder: 'e.g. 8000' },
    { key: 'radiusKm',          label: 'Radius (km)',         type: 'number', placeholder: 'e.g. 5' },
    { key: 'foodPreference',    label: 'Food Preference',     type: 'select', options: FOOD_OPTIONS },
    { key: 'smokingPreference', label: 'Smoking Preference',  type: 'select', options: SMOKING_OPTIONS },
    { key: 'preferredGender',   label: 'Preferred Gender',    type: 'select', options: GENDER_OPTIONS }
  ];

  // ── Private state ───────────────────────────────────────────────────
  private searchSub: Subscription | null = null;
  private activeFilters: TenantSearchFilters = {};
  private _clientSortByDistance = false;
  constructor(private readonly tenantService: TenantService) {}

  // ── Lifecycle ───────────────────────────────────────────────────────

  ngOnInit(): void {
    // Do NOT auto-fetch. Wait for the location picker to emit
    // (it will restore from localStorage if a previous session had one).
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
  }

  // ── Public API (used by template) ───────────────────────────────────

  /** Called by the location picker when user selects GPS or city. */
  onLocationSelected(location: ResolvedLocation | null): void {
    const prevLat = this.selectedLocation?.latitude;
    const prevLng = this.selectedLocation?.longitude;
    this.selectedLocation = location;

    // Location cleared → reset to "select location" state
    if (!location) {
      this.locationReady = false;
      this.tenantPosts = [];
      this.searchSub?.unsubscribe();
      this.loading = false;
      return;
    }

    this.locationReady = true;

    if (location.latitude === prevLat && location.longitude === prevLng) {
      return;
    }

    // Determine filtering strategy based on location type
    const locationType = location.type || 'city';
    const cityFilter = locationType === 'city' ? location.city || location.label : undefined;
    const areaFilter = locationType === 'area' ? location.area || location.label : undefined;

    const useRadiusFiltering = locationType === 'gps';
    const radiusKm = useRadiusFiltering
      ? (this.activeFilters.radiusKm ?? TenantMatchingPageComponent.DEFAULT_RADIUS_KM)
      : undefined;

    this.activeFilters = {
      ...this.activeFilters,
      latitude: location?.latitude,
      longitude: location?.longitude,
      radiusKm,
      city: cityFilter,
      area: areaFilter
    };
    this._clientSortByDistance = true;
    this.doSearch();
  }

  /** Called by the filters panel "Search" / "Reset" buttons. */
  search(raw: Record<string, unknown>): void {
    this.activeFilters = this.parseFilters(raw);
    this.doSearch();
    this.resultsPanel?.nativeElement?.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** trackBy for ngFor — prevents re-rendering cards that haven't changed. */
  trackByPostId(_index: number, post: TenantPost): number {
    return post.id;
  }

  /** Retry after error — re-runs current search. */
  doRetry(): void {
    this.doSearch();
  }

  // ── Private ─────────────────────────────────────────────────────────

  private doSearch(): void {
    this.searchSub?.unsubscribe();

    this.loading = true;
    this.errorMessage = null;

    this.searchSub = this.tenantService.searchTenantPosts(this.activeFilters).subscribe({
      next: (posts) => {
        let results = posts;
        if (this._clientSortByDistance) {
          results = [...results].sort((a, b) => {
            const da = a.distanceKm ?? Number.MAX_VALUE;
            const db = b.distanceKm ?? Number.MAX_VALUE;
            return da - db;
          });
        }
        this.tenantPosts = results;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to load available rooms.';
        this.loading = false;
      }
    });
  }

  private parseFilters(raw: Record<string, unknown>): TenantSearchFilters {
    const num = (v: unknown): number | undefined => {
      if (v === null || v === undefined || v === '') return undefined;
      const n = Number(v);
      return isNaN(n) ? undefined : n;
    };
    const str = (v: unknown): string | undefined => {
      if (v === null || v === undefined || v === '') return undefined;
      return String(v);
    };

    const hasLocation = this.selectedLocation?.latitude != null
      && this.selectedLocation?.longitude != null;

    const locationType = this.selectedLocation?.type || 'city';
    const useRadius = locationType === 'gps';

    const radiusKm = num(raw['radiusKm'])
      ?? (hasLocation && useRadius ? TenantMatchingPageComponent.DEFAULT_RADIUS_KM : undefined);

    const cityFilter = this.selectedLocation?.city;
    const areaFilter = this.selectedLocation?.area;

    const rawSort = str(raw['sortBy']);
    let sortBy: string | undefined;
    if (rawSort) {
      if (rawSort === 'distance,asc') {
        this._clientSortByDistance = true;
        sortBy = 'createdAt,desc';
      } else {
        this._clientSortByDistance = false;
        sortBy = rawSort;
      }
    } else {
      this._clientSortByDistance = !!(this.selectedLocation?.latitude);
    }

    return {
      budget:            num(raw['budget']),
      latitude:          this.selectedLocation?.latitude,
      longitude:         this.selectedLocation?.longitude,
      radiusKm,
      city:             locationType === 'city' ? cityFilter : undefined,
      area:             locationType === 'area' ? areaFilter : undefined,
      foodPreference:    str(raw['foodPreference']),
      smokingPreference: str(raw['smokingPreference']),
      preferredGender:   str(raw['preferredGender']),
      sortBy
    };
  }
}

