// ---------------------------------------------------------------------------
// Auth-related interfaces — mirrors the backend DTOs from API_CONTRACT.md
// ---------------------------------------------------------------------------

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: 'PG_OWNER' | 'TENANT' | 'ADMIN';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  userId: number;
  email: string;
  role: string;
  fullName?: string;
  phoneNumber?: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
}

/** Decoded payload stored alongside tokens for quick access. */
export interface AuthUser {
  userId: number;
  email: string;
  role: string;
  fullName?: string;
  phoneNumber?: string;
}

