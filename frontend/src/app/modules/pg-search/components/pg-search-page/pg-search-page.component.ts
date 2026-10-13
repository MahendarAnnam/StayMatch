import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { PgListing, PgSearchFilters } from '../../../../models/pg.model';
import { PgService } from '../../../../services/pg.service';
import { AuthService } from '../../../../services/auth.service';
import { FilterField, SelectOption } from '../../../../components/filters-panel/filters-panel.component';
import { ResolvedLocation } from '../../../../models/location.model';

/** Well-known facility names shown as checkboxes. */
const FACILITY_OPTIONS = [
  'WiFi', 'AC', 'Laundry', 'Parking', 'Gym',
  'Power Backup', 'Meals', 'TV', 'Geyser', 'Fridge'
];

const SHARING_TYPES = ['SINGLE', 'DOUBLE', 'TRIPLE', 'FOUR_SHARING'];

const PG_SORT_OPTIONS: SelectOption[] = [
  { label: 'Latest Posted',       value: 'createdAt,desc' },
  { label: 'Oldest Posted',       value: 'createdAt,asc' },
  { label: 'Price: Low → High',   value: 'price,asc' },
  { label: 'Price: High → Low',   value: 'price,desc' }
];

const FOOD_TYPE_OPTIONS: SelectOption[] = [
  { label: 'Pure Veg',       value: 'VEG' },
  { label: 'Non-Veg',        value: 'NON_VEG' },
  { label: 'Veg & Non-Veg',  value: 'BOTH' }
];

const PAGE_SIZE = 20;

@Component({
  selector: 'app-pg-search-page',
  templateUrl: './pg-search-page.component.html',
  styleUrls: ['./pg-search-page.component.css']
})
export class PgSearchPageComponent implements OnInit, OnDestroy {

  // ── Template-bound state ────────────────────────────────────────────
  @ViewChild('resultsPanel') private resultsPanel!: ElementRef<HTMLElement>;

  pgs: PgListing[] = [];
  loading = false;
  errorMessage: string | null = null;
  isLastPage = false;
  totalElements = 0;
  totalPages = 0;
  filtersOpen = false;

  /** True once the user has selected a location (mandatory before search). */
  locationReady = false;

  /** Location resolved from picker (GPS or city). */
  selectedLocation: ResolvedLocation | null = null;

  /** Filter panel configuration (location handled separately by picker). */
  filterFields: FilterField[] = [
    { key: 'sortBy',       label: 'Sort By',        type: 'select', options: PG_SORT_OPTIONS },
    { key: 'radiusKm',     label: 'Radius (km)',    type: 'number', placeholder: 'e.g. 5' },
    { key: 'minPrice',     label: 'Min Price (₹)',  type: 'number', placeholder: '0' },
    { key: 'maxPrice',     label: 'Max Price (₹)',  type: 'number', placeholder: '20000' },
    { key: 'sharingType',  label: 'Sharing Type',   type: 'select', options: SHARING_TYPES },
    { key: 'foodType',     label: 'Food Type',      type: 'select', options: FOOD_TYPE_OPTIONS },
    { key: 'facilityNames', label: 'Facilities',    type: 'checkbox-group', options: FACILITY_OPTIONS }
  ];

  // ── Private state ───────────────────────────────────────────────────
  private currentPage = 0;
  private activeFilters: PgSearchFilters = {};
  private searchSub: Subscription | null = null;
  private _clientSortByDistance = false;

  constructor(
    private readonly pgService: PgService,
    readonly authService: AuthService
  ) {}

  // ── Lifecycle ───────────────────────────────────────────────────────

  ngOnInit(): void {
    // Do NOT auto-fetch. Wait for the location picker to emit
    // (it will restore from localStorage if a previous session had one).
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
  }

  // ── Public API (used by template) ───────────────────────────────────

  /**
   * Loads the first page of results with the currently active filters.
   * Resets all pagination state and clears existing results.
   */
  loadInitial(): void {
    this.currentPage = 0;
    this.pgs = [];
    this.isLastPage = false;
    this.totalElements = 0;
    this.totalPages = 0;
    this.fetchPage(0);
  }

  /**
   * Appends the next page of results (infinite scroll).
   * No-ops if already loading or on the last page.
   */
  loadMore(): void {
    if (this.loading || this.isLastPage) {
      return;
    }
    this.fetchPage(this.currentPage + 1);
  }

  /** Called by the filters panel "Search" / "Reset" buttons. */
  resetSearch(raw: Record<string, unknown>): void {
    this.activeFilters = this.parseFilters(raw);
    this.loadInitial();
    this.resultsPanel?.nativeElement?.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Default search radius in km when a location is selected but no explicit radius set. */
  private static readonly DEFAULT_RADIUS_KM = 25;

  /** Called by the location picker when user selects GPS or city. */
  onLocationSelected(location: ResolvedLocation | null): void {
    const prevLat = this.selectedLocation?.latitude;
    const prevLng = this.selectedLocation?.longitude;
    this.selectedLocation = location;

    // Location cleared → reset to "select location" state
    if (!location) {
      this.locationReady = false;
      this.pgs = [];
      this.totalElements = 0;
      this.searchSub?.unsubscribe();
      this.loading = false;
      return;
    }

    this.locationReady = true;

    // Only re-search if lat/lng actually changed (skip address-only updates from reverse geocoding)
    if (location.latitude === prevLat && location.longitude === prevLng) {
      return;
    }

    // Determine filtering strategy based on location type
    const locationType = location.type || 'city';
    const cityFilter = locationType === 'city' ? location.city || location.label : undefined;
    const areaFilter = locationType === 'area' ? location.area || location.label : undefined;

    // For GPS: use radius-based filtering; for city/area: use exact match
    const useRadiusFiltering = locationType === 'gps';
    const radiusKm = useRadiusFiltering
      ? (this.activeFilters.radiusKm ?? PgSearchPageComponent.DEFAULT_RADIUS_KM)
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
    this.loadInitial();
  }

  /** trackBy for ngFor — prevents re-rendering cards that haven't changed. */
  trackByPgId(_index: number, pg: PgListing): number {
    return pg.id;
  }

  /** Returns true if the current user can create PG listings. */
  get canPostPg(): boolean {
    return this.authService.hasRole('PG_OWNER') || this.authService.hasRole('ADMIN');
  }

  // ── Infinite Scroll ─────────────────────────────────────────────────

  /** Called from template (scroll) event on the results panel. */
  onResultsScroll(): void {
    if (this.loading || this.isLastPage) {
      return;
    }

    const el = this.resultsPanel?.nativeElement;
    if (!el) return;

    // Trigger when within 300px of the bottom of the results container
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 300) {
      this.loadMore();
    }
  }

  // ── Private ─────────────────────────────────────────────────────────

  /**
   * Core fetch method. Cancels any in-flight request before starting
   * a new one to prevent duplicate / stale responses.
   */
  private fetchPage(page: number): void {
    // Cancel previous in-flight request (prevents race conditions)
    this.searchSub?.unsubscribe();

    this.loading = true;
    this.errorMessage = null;
    this.currentPage = page;

    this.searchSub = this.pgService
      .searchPgs(this.activeFilters, page, PAGE_SIZE)
      .subscribe({
        next: (res) => {
          let content = res.content;
          // Client-side distance sort
          if (this._clientSortByDistance) {
            content = [...content].sort((a, b) => {
              const da = a.distanceKm ?? Number.MAX_VALUE;
              const db = b.distanceKm ?? Number.MAX_VALUE;
              return da - db;
            });
          }
          // Page 0 → replace; subsequent pages → append
          this.pgs = page === 0 ? content : [...this.pgs, ...content];
          this.isLastPage = res.last;
          this.totalElements = res.totalElements;
          this.totalPages = res.totalPages;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage =
            err.error?.message ?? 'Failed to load PG listings. Please try again.';
          this.loading = false;
        }
      });
  }

  /** Merges filter-panel values with the selected location from the picker. */
  private parseFilters(raw: Record<string, unknown>): PgSearchFilters {
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

    // Determine if we should use radius or city-based filtering
    const locationType = this.selectedLocation?.type || 'city';
    const useRadius = locationType === 'gps';

    // Default radiusKm only for GPS-based location
    const radiusKm = num(raw['radiusKm'])
      ?? (hasLocation && useRadius ? PgSearchPageComponent.DEFAULT_RADIUS_KM : undefined);

    const cityFilter = this.selectedLocation?.city;
    const areaFilter = this.selectedLocation?.area;

    // Map frontend sort values to Spring Pageable sort format
    const rawSort = str(raw['sortBy']);
    let sortBy: string | undefined;
    let clientSortByDistance = false;
    if (rawSort) {
      if (rawSort === 'distance,asc') {
        // Distance sorting is done client-side (computed field)
        clientSortByDistance = true;
        sortBy = 'createdAt,desc'; // fallback backend sort
      } else if (rawSort.startsWith('price')) {
        sortBy = rawSort.replace('price', 'sharingOptions.rent');
      } else {
        sortBy = rawSort;
      }
    } else {
      // Default: sort by distance if location is available
      if (hasLocation) {
        clientSortByDistance = true;
      }
    }

    this._clientSortByDistance = clientSortByDistance;

    return {
      latitude:      this.selectedLocation?.latitude,
      longitude:     this.selectedLocation?.longitude,
      radiusKm,
      city:         locationType === 'city' ? cityFilter : undefined,
      area:         locationType === 'area' ? areaFilter : undefined,
      minPrice:     num(raw['minPrice']),
      maxPrice:     num(raw['maxPrice']),
      sharingType:  str(raw['sharingType']),
      foodType:     str(raw['foodType']),
      facilityNames: Array.isArray(raw['facilityNames']) && raw['facilityNames'].length > 0
        ? raw['facilityNames'] as string[]
        : undefined,
      sortBy
    };
  }
}
