export interface OwnerProfile {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
}

export interface SharingOption {
  id: number;
  sharingType: string;
  rent: number;
  availabilityCount: number;
}

export interface PgImage {
  id: number;
  imageUrl: string;
  displayOrder: number;
}

export interface PgListing {
  id: number;
  owner: OwnerProfile;
  name: string;
  description: string;
  latitude: number;
  longitude: number;
  locationLabel: string;
  fullAddress?: string;
  shortAddress?: string;
  addressCity?: string;
  addressState?: string;
  pincode?: string;
  isActive: boolean;
  facilities: string[];
  sharingOptions: SharingOption[];
  images: PgImage[];
  distanceKm: number | null;
  foodType?: 'VEG' | 'NON_VEG' | 'BOTH';
  weeklyMenu?: { [day: string]: string[] };
  nonVegDays?: string[];
  eggDays?: string[];
  priceType?: 'EXACT' | 'RANGE' | 'HIDDEN';
  minPrice?: number;
  maxPrice?: number;
  createdAt: string;
  updatedAt: string;
}

/** Mirrors Spring Boot Page<T> response structure. */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;      // current page (zero-based)
  first: boolean;
  last: boolean;
  empty: boolean;
}

/** Filter parameters sent to GET /api/v1/pgs/search. */
export interface PgSearchFilters {
  latitude?: number;
  longitude?: number;
  radiusKm?: number;
  city?: string;
  area?: string;
  sharingType?: string;
  minPrice?: number;
  maxPrice?: number;
  facilityNames?: string[];
  foodType?: string;
  sortBy?: string;        // e.g. "createdAt,desc", "price,asc"
}

