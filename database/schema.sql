BEGIN;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS owner_profiles (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pg (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES owner_profiles(id),
    name VARCHAR(150) NOT NULL,
    description VARCHAR(600),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location_label VARCHAR(150) NOT NULL,
    full_address VARCHAR(500),
    short_address VARCHAR(200),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    pincode VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    food_type VARCHAR(20),
    weekly_menu TEXT,
    non_veg_days TEXT,
    egg_days TEXT,
    price_type VARCHAR(20) NOT NULL DEFAULT 'EXACT',
    min_price NUMERIC(10,2),
    max_price NUMERIC(10,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_price_range CHECK (min_price IS NULL OR max_price IS NULL OR min_price <= max_price)
);

CREATE TABLE IF NOT EXISTS sharing_options (
    id BIGSERIAL PRIMARY KEY,
    pg_id BIGINT NOT NULL REFERENCES pg(id) ON DELETE CASCADE,
    sharing_type VARCHAR(40) NOT NULL,
    rent NUMERIC(10,2) NOT NULL CHECK (rent > 0),
    availability_count INTEGER NOT NULL CHECK (availability_count >= 0),
    UNIQUE(pg_id, sharing_type)
);

CREATE TABLE IF NOT EXISTS facilities (
    id BIGSERIAL PRIMARY KEY,
    facility_name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pg_facilities (
    id BIGSERIAL PRIMARY KEY,
    pg_id BIGINT NOT NULL REFERENCES pg(id) ON DELETE CASCADE,
    facility_id BIGINT NOT NULL REFERENCES facilities(id),
    UNIQUE (pg_id, facility_id)
);

CREATE TABLE IF NOT EXISTS images (
    id BIGSERIAL PRIMARY KEY,
    pg_id BIGINT NOT NULL REFERENCES pg(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS preferences (
    id BIGSERIAL PRIMARY KEY,
    food_preference VARCHAR(50),
    smoking_preference VARCHAR(50),
    preferred_gender VARCHAR(50),
    additional_notes VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS tenant_posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    preference_id BIGINT UNIQUE REFERENCES preferences(id) ON DELETE SET NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    min_budget NUMERIC(10,2) NOT NULL CHECK (min_budget > 0),
    max_budget NUMERIC(10,2) NOT NULL CHECK (max_budget >= min_budget),
    preferred_latitude DOUBLE PRECISION NOT NULL,
    preferred_longitude DOUBLE PRECISION NOT NULL,
    location_label VARCHAR(120) NOT NULL,
    full_address VARCHAR(500),
    short_address VARCHAR(200),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    pincode VARCHAR(20),
    move_in_date DATE NOT NULL,
    total_capacity INTEGER NOT NULL DEFAULT 1 CHECK (total_capacity >= 1),
    current_occupancy INTEGER NOT NULL DEFAULT 0 CHECK (current_occupancy >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_occupancy_lte_capacity CHECK (current_occupancy <= total_capacity)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pg_geo ON pg (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_pg_active ON pg (is_active);
CREATE INDEX IF NOT EXISTS idx_sharing_option_filter ON sharing_options (sharing_type, rent);
CREATE INDEX IF NOT EXISTS idx_pg_facilities_facility ON pg_facilities (facility_id);
CREATE INDEX IF NOT EXISTS idx_tenant_post_budget ON tenant_posts (min_budget, max_budget);
CREATE INDEX IF NOT EXISTS idx_tenant_post_geo ON tenant_posts (preferred_latitude, preferred_longitude);
CREATE INDEX IF NOT EXISTS idx_tenant_post_active_movein ON tenant_posts (is_active, move_in_date);
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens (user_id);

COMMIT;

