export interface TenantUser {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
}

export interface TenantPreference {
  id?: number;
  foodPreference?: string;
  smokingPreference?: string;
  preferredGender?: string;
  additionalNotes?: string;
}

export interface TenantPost {
  id: number;
  user: TenantUser;
  title: string;
  description: string;
  minBudget: number;
  maxBudget: number;
  preferredLatitude: number;
  preferredLongitude: number;
  locationLabel: string;
  fullAddress?: string;
  shortAddress?: string;
  addressCity?: string;
  addressState?: string;
  pincode?: string;
  moveInDate: string;
  totalCapacity: number;
  currentOccupancy: number;
  availableSpots: number;        // computed by backend
  isActive: boolean;
  preference: TenantPreference;
  distanceKm?: number;
  matchScore?: number;
  createdAt: string;
  updatedAt: string;
}

/** Search filter parameters for GET /api/v1/tenant-posts/search */
export interface TenantSearchFilters {
  budget?: number;
  latitude?: number;
  longitude?: number;
  radiusKm?: number;
  city?: string;
  area?: string;
  foodPreference?: string;
  smokingPreference?: string;
  preferredGender?: string;
  sortBy?: string;        // e.g. "createdAt,desc", "price,asc"
}

/** Request body for POST /api/v1/tenant-posts */
export interface CreateTenantPostRequest {
  user: {
    fullName: string;
    email: string;
    phoneNumber?: string;
  };
  title: string;
  description?: string;
  minBudget: number;
  maxBudget: number;
  preferredLatitude: number;
  preferredLongitude: number;
  locationLabel: string;
  fullAddress?: string;
  shortAddress?: string;
  addressCity?: string;
  addressState?: string;
  pincode?: string;
  moveInDate: string;               // YYYY-MM-DD
  totalCapacity: number;
  currentOccupancy: number;
  preference: {
    foodPreference?: string;
    smokingPreference?: string;
    preferredGender?: string;
    additionalNotes?: string;
  };
}

