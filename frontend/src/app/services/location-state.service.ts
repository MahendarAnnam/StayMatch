import { ResolvedLocation } from '../models/location.model';

/** Default localStorage key — used as fallback if none specified. */
const DEFAULT_KEY = 'staymatch_location';

/**
 * Scoped, persistent location state.
 *
 * Each instance manages its own localStorage key so that PG and Tenant
 * modules do NOT share location state.
 *
 * This is NOT provided at root level — each LocationPickerComponent
 * creates its own instance based on the storageKey input.
 */
export class LocationStateService {

  private readonly storageKey: string;
  private currentLocation: ResolvedLocation | null;

  constructor(storageKey?: string) {
    this.storageKey = storageKey || DEFAULT_KEY;
    this.currentLocation = this.loadFromStorage();
  }

  /** Snapshot of the current location value. */
  get current(): ResolvedLocation | null {
    return this.currentLocation;
  }

  /** Update the location, persist to localStorage. */
  set(location: ResolvedLocation | null): void {
    if (location) {
      localStorage.setItem(this.storageKey, JSON.stringify(location));
    } else {
      localStorage.removeItem(this.storageKey);
    }
    this.currentLocation = location;
  }

  /** Clear the stored location. */
  clear(): void {
    this.set(null);
  }

  // ── Private ─────────────────────────────────────────────────────────

  private loadFromStorage(): ResolvedLocation | null {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as ResolvedLocation;
      // Validate minimum required fields
      if (parsed?.latitude != null && parsed?.longitude != null && parsed?.label) {
        return parsed;
      }
      return null;
    } catch {
      return null;
    }
  }
}

