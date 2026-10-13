import { LocationStateService } from './location-state.service';
import { ResolvedLocation } from '../models/location.model';

describe('LocationStateService', () => {

  const pgKey = 'test_pg_location';
  const tenantKey = 'test_tenant_location';

  const bangaloreLocation: ResolvedLocation = {
    latitude: 12.9716,
    longitude: 77.5946,
    label: 'Bangalore'
  };

  const hyderabadLocation: ResolvedLocation = {
    latitude: 17.385,
    longitude: 78.4867,
    label: 'Hyderabad'
  };

  beforeEach(() => {
    // Clear test keys before each test
    localStorage.removeItem(pgKey);
    localStorage.removeItem(tenantKey);
  });

  afterAll(() => {
    localStorage.removeItem(pgKey);
    localStorage.removeItem(tenantKey);
  });

  // ── Basic functionality ───────────────────────────────────────────

  it('should return null when no location is stored', () => {
    const service = new LocationStateService(pgKey);
    expect(service.current).toBeNull();
  });

  it('should store and retrieve a location', () => {
    const service = new LocationStateService(pgKey);
    service.set(bangaloreLocation);

    expect(service.current).toEqual(bangaloreLocation);
  });

  it('should persist location to localStorage', () => {
    const service = new LocationStateService(pgKey);
    service.set(bangaloreLocation);

    const stored = JSON.parse(localStorage.getItem(pgKey)!);
    expect(stored.latitude).toBe(12.9716);
    expect(stored.longitude).toBe(77.5946);
    expect(stored.label).toBe('Bangalore');
  });

  it('should restore location from localStorage on construction', () => {
    localStorage.setItem(pgKey, JSON.stringify(bangaloreLocation));

    const service = new LocationStateService(pgKey);
    expect(service.current).toEqual(bangaloreLocation);
  });

  it('should clear stored location', () => {
    const service = new LocationStateService(pgKey);
    service.set(bangaloreLocation);
    expect(service.current).not.toBeNull();

    service.clear();
    expect(service.current).toBeNull();
    expect(localStorage.getItem(pgKey)).toBeNull();
  });

  it('should remove localStorage key when setting null', () => {
    const service = new LocationStateService(pgKey);
    service.set(bangaloreLocation);
    service.set(null);

    expect(localStorage.getItem(pgKey)).toBeNull();
    expect(service.current).toBeNull();
  });

  // ── Scoped isolation (PG vs Tenant) ──────────────────────────────

  it('should isolate PG location from Tenant location', () => {
    const pgService = new LocationStateService(pgKey);
    const tenantService = new LocationStateService(tenantKey);

    pgService.set(bangaloreLocation);
    tenantService.set(hyderabadLocation);

    expect(pgService.current!.label).toBe('Bangalore');
    expect(tenantService.current!.label).toBe('Hyderabad');
  });

  it('should NOT leak PG location change to Tenant service', () => {
    const pgService = new LocationStateService(pgKey);
    const tenantService = new LocationStateService(tenantKey);

    tenantService.set(hyderabadLocation);
    pgService.set(bangaloreLocation);

    // Tenant should still be Hyderabad
    expect(tenantService.current!.label).toBe('Hyderabad');
  });

  it('should NOT leak Tenant clear to PG service', () => {
    const pgService = new LocationStateService(pgKey);
    const tenantService = new LocationStateService(tenantKey);

    pgService.set(bangaloreLocation);
    tenantService.set(hyderabadLocation);

    tenantService.clear();

    expect(tenantService.current).toBeNull();
    expect(pgService.current!.label).toBe('Bangalore');
  });

  it('should use separate localStorage keys', () => {
    const pgService = new LocationStateService(pgKey);
    const tenantService = new LocationStateService(tenantKey);

    pgService.set(bangaloreLocation);
    tenantService.set(hyderabadLocation);

    const pgStored = JSON.parse(localStorage.getItem(pgKey)!);
    const tenantStored = JSON.parse(localStorage.getItem(tenantKey)!);

    expect(pgStored.label).toBe('Bangalore');
    expect(tenantStored.label).toBe('Hyderabad');
  });

  // ── Edge cases ───────────────────────────────────────────────────

  it('should return null for corrupted localStorage data', () => {
    localStorage.setItem(pgKey, 'not-valid-json');
    const service = new LocationStateService(pgKey);
    expect(service.current).toBeNull();
  });

  it('should return null for incomplete location data in localStorage', () => {
    localStorage.setItem(pgKey, JSON.stringify({ latitude: 12.97 }));
    const service = new LocationStateService(pgKey);
    expect(service.current).toBeNull();
  });

  it('should return null when label is missing', () => {
    localStorage.setItem(pgKey, JSON.stringify({ latitude: 12.97, longitude: 77.59 }));
    const service = new LocationStateService(pgKey);
    expect(service.current).toBeNull();
  });

  it('should accept location with extra fields', () => {
    const enrichedLocation: ResolvedLocation = {
      latitude: 12.97,
      longitude: 77.59,
      label: 'Bangalore',
      address: 'Koramangala, Bangalore (560034)',
      area: 'Koramangala'
    };
    const service = new LocationStateService(pgKey);
    service.set(enrichedLocation);

    expect(service.current!.address).toBe('Koramangala, Bangalore (560034)');
    expect(service.current!.area).toBe('Koramangala');
  });
});

