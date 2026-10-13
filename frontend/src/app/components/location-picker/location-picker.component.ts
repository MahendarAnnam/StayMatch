import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, of, Subscription, EMPTY } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, filter, tap } from 'rxjs/operators';
import { LocationSuggestion, ResolvedLocation } from '../../models/location.model';
import { LocationService } from '../../services/location.service';
import { LocationStateService } from '../../services/location-state.service';

/** Minimum characters before triggering autocomplete search. */
const MIN_QUERY_LENGTH = 3;
/** Debounce time in ms after user stops typing. */
const DEBOUNCE_MS = 400;
/** Maximum suggestions shown in dropdown. */
const MAX_SUGGESTIONS = 5;

@Component({
  selector: 'app-location-picker',
  templateUrl: './location-picker.component.html',
  styleUrls: ['./location-picker.component.css']
})
export class LocationPickerComponent implements OnInit, OnDestroy {

  /**
   * Scoping key for localStorage persistence.
   */
  @Input() storageKey = 'staymatch_location';

  @Output() locationSelected = new EventEmitter<ResolvedLocation | null>();

  /** Search input form control for autocomplete. */
  searchControl = new FormControl('');

  /** Autocomplete suggestions. */
  suggestions: LocationSuggestion[] = [];
  loadingSuggestions = false;

  /** Currently resolved location (null = cleared). */
  resolved: ResolvedLocation | null = null;
  geoLoading = false;
  geoError: string | null = null;
  addressLoading = false;

  /** Hint shown below the input. */
  get searchHint(): string {
    const val = this.searchControl.value;
    if (!val || (typeof val === 'string' && val.length > 0 && val.length < MIN_QUERY_LENGTH)) {
      return `Type at least ${MIN_QUERY_LENGTH} characters to search`;
    }
    return '';
  }

  private sub: Subscription | null = null;
  private geocodeSub: Subscription | null = null;
  private searchSub: Subscription | null = null;
  private detailsSub: Subscription | null = null;
  private locationState!: LocationStateService;

  /** Cached initial cities (loaded once on init). */
  private cachedCities: LocationSuggestion[] = [];

  constructor(
    private readonly locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.locationState = new LocationStateService(this.storageKey);

    // Load initial cities once for empty-state dropdown
    this.locationService.searchLocations('', 18).subscribe({
      next: (results) => {
        this.cachedCities = results;
        // Show cities initially if no resolved location
        if (!this.resolved) {
          this.suggestions = results;
        }
      },
      error: () => {}
    });

    // Setup optimized autocomplete pipeline:
    // - debounce 400ms
    // - only trigger on >= 3 chars (show cached cities for < 3)
    // - switchMap cancels previous in-flight request
    // - limit to 5 results
    this.searchSub = this.searchControl.valueChanges.pipe(
      debounceTime(DEBOUNCE_MS),
      distinctUntilChanged(),
      filter((val): val is string => typeof val === 'string'),
      tap((query) => {
        // Show cached cities immediately for short queries (no API call)
        if (query.length < MIN_QUERY_LENGTH) {
          this.suggestions = this.cachedCities;
          this.loadingSuggestions = false;
        }
      }),
      // Only call API when query >= MIN_QUERY_LENGTH
      switchMap((query) => {
        if (query.length < MIN_QUERY_LENGTH) {
          return EMPTY; // already handled in tap above
        }
        this.loadingSuggestions = true;
        return this.locationService.searchLocations(query, MAX_SUGGESTIONS).pipe(
          catchError(() => {
            this.loadingSuggestions = false;
            return of([] as LocationSuggestion[]);
          })
        );
      })
    ).subscribe({
      next: (results) => {
        this.suggestions = results;
        this.loadingSuggestions = false;
      },
      error: () => {
        this.loadingSuggestions = false;
      }
    });

    // Restore persisted location
    const persisted = this.locationState.current;
    if (persisted) {
      this.resolved = persisted;
      this.locationSelected.emit(persisted);
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.geocodeSub?.unsubscribe();
    this.searchSub?.unsubscribe();
    this.detailsSub?.unsubscribe();
  }

  // ── Actions ─────────────────────────────────────────────────────────

  /**
   * Uses the service's getCurrentLocation() which fetches GPS coordinates
   * and automatically enriches with reverse-geocoded address in one call.
   */
  useMyLocation(): void {
    this.sub?.unsubscribe();
    this.geoLoading = true;
    this.addressLoading = true;
    this.geoError = null;

    this.sub = this.locationService.getCurrentLocation().subscribe({
      next: (loc) => {
        loc.type = 'gps';
        this.resolved = loc;
        this.geoLoading = false;
        this.addressLoading = false;
        this.searchControl.setValue('', { emitEvent: false });
        this.suggestions = this.cachedCities;
        this.locationState.set(loc);
        this.locationSelected.emit(loc);
      },
      error: (err) => {
        this.geoError = typeof err === 'string' ? err : 'Failed to get location.';
        this.geoLoading = false;
        this.addressLoading = false;
      }
    });
  }

  /**
   * Called when user selects a suggestion from autocomplete.
   * If suggestion has placeId → call Place Details API to resolve lat/lng.
   * If suggestion already has lat/lng → use directly.
   */
  onSuggestionSelected(suggestion: LocationSuggestion): void {
    this.geoError = null;
    this.searchControl.setValue('', { emitEvent: false });
    this.suggestions = this.cachedCities;

    if (suggestion.placeId && (suggestion.lat == null || suggestion.lng == null)) {
      // Resolve via Place Details API
      this.addressLoading = true;
      this.detailsSub?.unsubscribe();

      this.detailsSub = this.locationService.getPlaceDetails(suggestion.placeId).subscribe({
        next: (details) => {
          const loc: ResolvedLocation = {
            latitude: details.lat,
            longitude: details.lng,
            label: details.label || suggestion.label,
            fullAddress: details.fullAddress,
            city: details.city,
            area: details.area || undefined,
            state: details.state,
            pincode: details.pincode,
            type: details.type as 'city' | 'area'
          };
          this.resolved = loc;
          this.addressLoading = false;
          this.locationState.set(loc);
          this.locationSelected.emit(loc);
        },
        error: () => {
          this.geoError = 'Failed to resolve location details. Please try again.';
          this.addressLoading = false;
        }
      });
    } else {
      // Already has coordinates (DB-based city suggestion)
      const loc: ResolvedLocation = {
        latitude: suggestion.lat!,
        longitude: suggestion.lng!,
        label: suggestion.label,
        city: suggestion.city || undefined,
        area: suggestion.area || undefined,
        type: suggestion.type as 'city' | 'area'
      };
      this.resolveAddress(loc);
    }
  }

  /**
   * Display function for autocomplete.
   */
  displayFn(suggestion: LocationSuggestion | string): string {
    if (!suggestion) return '';
    return typeof suggestion === 'string' ? suggestion : suggestion.label;
  }

  clearLocation(): void {
    this.geocodeSub?.unsubscribe();
    this.detailsSub?.unsubscribe();
    this.resolved = null;
    this.geoError = null;
    this.addressLoading = false;
    this.searchControl.setValue('', { emitEvent: false });
    this.suggestions = this.cachedCities;
    this.locationState.clear();
    this.locationSelected.emit(null);
  }

  // ── Private ─────────────────────────────────────────────────────────

  private resolveAddress(loc: ResolvedLocation): void {
    this.resolved = loc;
    this.locationState.set(loc);
    this.locationSelected.emit(this.resolved);

    this.geocodeSub?.unsubscribe();
    this.addressLoading = true;

    this.geocodeSub = this.locationService
      .resolveLocationWithAddress(loc)
      .subscribe({
        next: (enriched) => {
          enriched.type = loc.type;
          enriched.city = enriched.city || loc.city;
          enriched.area = enriched.area || loc.area;
          this.resolved = enriched;
          this.addressLoading = false;
          this.locationState.set(enriched);
          this.locationSelected.emit(this.resolved);
        },
        error: () => {
          this.addressLoading = false;
        }
      });
  }
}
