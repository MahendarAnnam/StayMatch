-- ============================================================
-- StayMatch – H2 Seed Data (local development)
-- MODE=PostgreSQL; uses MERGE ... KEY() for H2 idempotency
-- ============================================================

-- ── Cities ──────────────────────────────────────────────────
MERGE INTO cities (name, latitude, longitude, created_at, updated_at) KEY (name) VALUES
  ('Bangalore',    12.9716,  77.5946, NOW(), NOW()),
  ('Hyderabad',    17.3850,  78.4867, NOW(), NOW()),
  ('Mumbai',       19.0760,  72.8777, NOW(), NOW()),
  ('Delhi',        28.7041,  77.1025, NOW(), NOW()),
  ('Chennai',      13.0827,  80.2707, NOW(), NOW()),
  ('Pune',         18.5204,  73.8567, NOW(), NOW()),
  ('Kolkata',      22.5726,  88.3639, NOW(), NOW()),
  ('Ahmedabad',    23.0225,  72.5714, NOW(), NOW()),
  ('Jaipur',       26.9124,  75.7873, NOW(), NOW()),
  ('Lucknow',      26.8467,  80.9462, NOW(), NOW()),
  ('Chandigarh',   30.7333,  76.7794, NOW(), NOW()),
  ('Kochi',         9.9312,  76.2673, NOW(), NOW()),
  ('Indore',       22.7196,  75.8577, NOW(), NOW()),
  ('Noida',        28.5355,  77.3910, NOW(), NOW()),
  ('Gurgaon',      28.4595,  77.0266, NOW(), NOW()),
  ('Coimbatore',   11.0168,  76.9558, NOW(), NOW()),
  ('Visakhapatnam',17.6868,  83.2185, NOW(), NOW()),
  ('Mysore',       12.2958,  76.6394, NOW(), NOW());

-- ── Facilities (master list) ────────────────────────────────
MERGE INTO facilities (facility_name) KEY (facility_name) VALUES
  ('WiFi'), ('AC'), ('Laundry'), ('Parking'), ('Gym'),
  ('Power Backup'), ('Meals'), ('TV'), ('Geyser'), ('Fridge'),
  ('Water Purifier'), ('CCTV'), ('Housekeeping'), ('Lift');

-- ── Owner Profiles ──────────────────────────────────────────
MERGE INTO owner_profiles (id, full_name, email, phone_number, created_at, updated_at) KEY (email) VALUES
  (1,  'Ramesh Kumar',   'ramesh.kumar@staymatch.dev',   '9876543210', NOW(), NOW()),
  (2,  'Priya Sharma',   'priya.sharma@staymatch.dev',   '9876543211', NOW(), NOW()),
  (3,  'Suresh Reddy',   'suresh.reddy@staymatch.dev',   '9876543212', NOW(), NOW()),
  (4,  'Anjali Mehta',   'anjali.mehta@staymatch.dev',   '9876543213', NOW(), NOW()),
  (5,  'Vikram Singh',   'vikram.singh@staymatch.dev',   '9876543214', NOW(), NOW());
ALTER TABLE owner_profiles ALTER COLUMN id RESTART WITH 100;

-- ── PG Entries ──────────────────────────────────────────────
-- Bangalore PGs
MERGE INTO pg (id, owner_id, name, description, latitude, longitude, location_label, full_address, short_address, address_city, address_state, pincode, is_active, food_type, weekly_menu, non_veg_days, egg_days, price_type, min_price, max_price, created_at, updated_at) KEY (id) VALUES
  (1,  1, 'Green Nest PG',
       'Spacious rooms in the heart of Koramangala with home-cooked meals and 24/7 WiFi.',
       12.9352, 77.6245, 'Koramangala, Bangalore (560034)',
       'Koramangala 4th Block, Koramangala, Bangalore, Karnataka 560034, India',
       'Koramangala, Bangalore', 'Bangalore', 'Karnataka', '560034', TRUE,
       'VEG',
       '{"MONDAY":["Poha","Dal Rice","Roti Sabzi"],"TUESDAY":["Upma","Rajma Rice","Chapati Dal"],"WEDNESDAY":["Idli Sambar","Chole Rice","Roti Paneer"],"THURSDAY":["Dosa","Sambar Rice","Chapati Sabzi"],"FRIDAY":["Paratha","Dal Rice","Roti Paneer"],"SATURDAY":["Puri Sabzi","Biryani Veg","Roti Dal"],"SUNDAY":["Chole Bhature","Pulao","Roti Sabzi"]}',
       NULL, NULL,
       'EXACT', NULL, NULL, NOW(), NOW()),
  (2,  2, 'Sunshine Living',
       'Modern PG near Indiranagar metro with gym and rooftop garden.',
       12.9716, 77.6412, 'Indiranagar, Bangalore (560038)',
       'Indiranagar, Bangalore, Karnataka 560038, India',
       'Indiranagar, Bangalore', 'Bangalore', 'Karnataka', '560038', TRUE,
       'BOTH',
       '{"MONDAY":["Poha","Dal Rice","Roti Chicken"],"TUESDAY":["Idli","Egg Curry Rice","Chapati Dal"],"WEDNESDAY":["Upma","Chicken Biryani","Roti Sabzi"],"THURSDAY":["Dosa","Dal Rice","Roti Egg Bhurji"],"FRIDAY":["Paratha","Fish Curry Rice","Chapati Sabzi"],"SATURDAY":["Puri","Mutton Biryani","Roti Dal"],"SUNDAY":["Chole Bhature","Chicken Pulao","Roti Paneer"]}',
       '["MONDAY","WEDNESDAY","SATURDAY","SUNDAY"]',
       '["TUESDAY","THURSDAY"]',
       'RANGE', 8000.00, 12000.00, NOW(), NOW()),
  (3,  1, 'Cloud9 Residency',
       'Premium co-living space in HSR Layout with AC rooms and parking.',
       12.9116, 77.6389, 'HSR Layout, Bangalore (560102)',
       'HSR Layout, Bangalore, Karnataka 560102, India',
       'HSR Layout, Bangalore', 'Bangalore', 'Karnataka', '560102', TRUE,
       'BOTH',
       '{"MONDAY":["Poha","Dal Rice","Roti Sabzi"],"TUESDAY":["Dosa","Rajma Rice","Chapati Egg Curry"],"WEDNESDAY":["Idli","Chicken Biryani","Roti Dal"],"THURSDAY":["Upma","Dal Rice","Chapati Paneer"],"FRIDAY":["Paratha","Fish Fry Rice","Roti Sabzi"],"SATURDAY":["Puri Sabzi","Veg Biryani","Roti Dal"],"SUNDAY":["Chole Bhature","Chicken Pulao","Roti Paneer"]}',
       '["WEDNESDAY","FRIDAY","SUNDAY"]',
       '["TUESDAY"]',
       'HIDDEN', NULL, NULL, NOW(), NOW()),

-- Hyderabad PGs
  (4,  3, 'Kondapur Heights',
       'Affordable PG near IT corridor with meals and laundry service.',
       17.4600, 78.3548, 'Kondapur, Hyderabad (500084)',
       'Kondapur, Hyderabad, Telangana 500084, India',
       'Kondapur, Hyderabad', 'Hyderabad', 'Telangana', '500084', TRUE,
       'BOTH',
       '{"MONDAY":["Upma","Dal Rice","Roti Sabzi"],"TUESDAY":["Idli","Egg Biryani","Chapati Dal"],"WEDNESDAY":["Poha","Chicken Curry Rice","Roti Sabzi"],"THURSDAY":["Dosa","Dal Rice","Chapati Paneer"],"FRIDAY":["Paratha","Fish Curry Rice","Roti Sabzi"],"SATURDAY":["Puri","Mutton Biryani","Roti Dal"],"SUNDAY":["Pesarattu","Chicken Biryani","Roti Paneer"]}',
       '["WEDNESDAY","FRIDAY","SATURDAY","SUNDAY"]',
       '["TUESDAY"]',
       'EXACT', NULL, NULL, NOW(), NOW()),
  (5,  3, 'Gachibowli Stay',
       'Budget-friendly rooms near Financial District with power backup.',
       17.4401, 78.3489, 'Gachibowli, Hyderabad (500032)',
       'Gachibowli, Hyderabad, Telangana 500032, India',
       'Gachibowli, Hyderabad', 'Hyderabad', 'Telangana', '500032', TRUE,
       'NON_VEG',
       '{"MONDAY":["Bread Omelette","Chicken Rice","Roti Egg Curry"],"TUESDAY":["Upma","Fish Fry Rice","Chapati Chicken"],"WEDNESDAY":["Dosa","Mutton Biryani","Roti Sabzi"],"THURSDAY":["Idli","Chicken Curry Rice","Chapati Egg Bhurji"],"FRIDAY":["Paratha","Fish Biryani","Roti Chicken"],"SATURDAY":["Poha","Chicken Biryani","Roti Sabzi"],"SUNDAY":["Puri","Mutton Curry Rice","Roti Paneer"]}',
       '["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"]',
       '["MONDAY","THURSDAY"]',
       'RANGE', 3000.00, 4500.00, NOW(), NOW()),
  (6,  4, 'Madhapur Comfort PG',
       'Fully furnished PG in Madhapur with AC, WiFi, and housekeeping.',
       17.4484, 78.3908, 'Madhapur, Hyderabad (500081)',
       'Madhapur, Hyderabad, Telangana 500081, India',
       'Madhapur, Hyderabad', 'Hyderabad', 'Telangana', '500081', TRUE,
       'VEG',
       '{"MONDAY":["Poha","Dal Rice","Roti Paneer"],"TUESDAY":["Upma","Rajma Rice","Chapati Sabzi"],"WEDNESDAY":["Idli Sambar","Chole Rice","Roti Dal"],"THURSDAY":["Dosa","Sambar Rice","Chapati Paneer"],"FRIDAY":["Paratha","Dal Rice","Roti Sabzi"],"SATURDAY":["Puri Sabzi","Veg Biryani","Roti Dal"],"SUNDAY":["Chole Bhature","Veg Pulao","Roti Paneer"]}',
       NULL, NULL,
       'EXACT', NULL, NULL, NOW(), NOW()),

-- Mumbai PGs
  (7,  4, 'Marine Drive PG',
       'Sea-facing rooms near Churchgate with breakfast included.',
       18.9440, 72.8237, 'Marine Drive, Mumbai (400020)',
       'Marine Drive, Mumbai, Maharashtra 400020, India',
       'Marine Drive, Mumbai', 'Mumbai', 'Maharashtra', '400020', TRUE,
       'BOTH',
       '{"MONDAY":["Poha","Dal Rice","Roti Chicken"],"TUESDAY":["Upma","Egg Curry Rice","Chapati Sabzi"],"WEDNESDAY":["Idli","Fish Curry Rice","Roti Dal"],"THURSDAY":["Dosa","Chicken Biryani","Chapati Paneer"],"FRIDAY":["Paratha","Fish Fry Rice","Roti Sabzi"],"SATURDAY":["Puri","Mutton Biryani","Roti Dal"],"SUNDAY":["Vada Pav","Chicken Pulao","Roti Paneer"]}',
       '["MONDAY","WEDNESDAY","THURSDAY","SATURDAY","SUNDAY"]',
       '["TUESDAY"]',
       'HIDDEN', NULL, NULL, NOW(), NOW()),
  (8,  5, 'Andheri Nest',
       'Well-connected PG near Andheri station with laundry and meals.',
       19.1197, 72.8464, 'Andheri West, Mumbai (400058)',
       'Andheri West, Mumbai, Maharashtra 400058, India',
       'Andheri West, Mumbai', 'Mumbai', 'Maharashtra', '400058', TRUE,
       'VEG',
       '{"MONDAY":["Poha","Dal Rice","Roti Sabzi"],"TUESDAY":["Upma","Rajma Rice","Chapati Paneer"],"WEDNESDAY":["Idli","Chole Rice","Roti Dal"],"THURSDAY":["Dosa","Dal Rice","Chapati Sabzi"],"FRIDAY":["Paratha","Kadhi Rice","Roti Paneer"],"SATURDAY":["Puri Sabzi","Veg Biryani","Roti Dal"],"SUNDAY":["Misal Pav","Veg Pulao","Roti Sabzi"]}',
       NULL, NULL,
       'EXACT', NULL, NULL, NOW(), NOW()),

-- Pune PGs
  (9,  5, 'Hinjewadi Hub PG',
       'PG for IT professionals near Hinjewadi IT Park with gym and WiFi.',
       18.5912, 73.7390, 'Hinjewadi, Pune (411057)',
       'Hinjewadi, Pune, Maharashtra 411057, India',
       'Hinjewadi, Pune', 'Pune', 'Maharashtra', '411057', TRUE,
       'BOTH',
       '{"MONDAY":["Poha","Dal Rice","Roti Chicken"],"TUESDAY":["Upma","Egg Biryani","Chapati Sabzi"],"WEDNESDAY":["Sabudana Khichdi","Chicken Curry Rice","Roti Dal"],"THURSDAY":["Idli","Dal Rice","Chapati Egg Curry"],"FRIDAY":["Paratha","Fish Fry Rice","Roti Paneer"],"SATURDAY":["Thalipeeth","Mutton Biryani","Roti Sabzi"],"SUNDAY":["Misal Pav","Chicken Pulao","Roti Paneer"]}',
       '["MONDAY","WEDNESDAY","SATURDAY","SUNDAY"]',
       '["TUESDAY","THURSDAY"]',
       'RANGE', 6000.00, 8500.00, NOW(), NOW()),
  (10, 2, 'Kothrud Comfort',
       'Quiet PG in Kothrud residential area with home-cooked veg meals.',
       18.5074, 73.8077, 'Kothrud, Pune (411038)',
       'Kothrud, Pune, Maharashtra 411038, India',
       'Kothrud, Pune', 'Pune', 'Maharashtra', '411038', TRUE,
       'VEG',
       '{"MONDAY":["Poha","Dal Rice","Roti Sabzi"],"TUESDAY":["Upma","Rajma Rice","Chapati Paneer"],"WEDNESDAY":["Sabudana Khichdi","Chole Rice","Roti Dal"],"THURSDAY":["Idli","Sambar Rice","Chapati Sabzi"],"FRIDAY":["Thalipeeth","Dal Rice","Roti Paneer"],"SATURDAY":["Puri Sabzi","Veg Biryani","Roti Dal"],"SUNDAY":["Misal Pav","Veg Pulao","Roti Sabzi"]}',
       NULL, NULL,
       'EXACT', NULL, NULL, NOW(), NOW());
ALTER TABLE pg ALTER COLUMN id RESTART WITH 100;

-- ── Sharing Options ─────────────────────────────────────────
-- PG 1: Green Nest PG
MERGE INTO sharing_options (id, pg_id, sharing_type, rent, availability_count) KEY (id) VALUES
  (1,  1, 'SINGLE',        9500.00,  3),
  (2,  1, 'DOUBLE',        6500.00,  5),
  (3,  1, 'TRIPLE',        5000.00,  4),

-- PG 2: Sunshine Living
  (4,  2, 'SINGLE',       12000.00,  2),
  (5,  2, 'DOUBLE',        8000.00,  6),

-- PG 3: Cloud9 Residency
  (6,  3, 'SINGLE',       14000.00,  4),
  (7,  3, 'DOUBLE',        9500.00,  3),
  (8,  3, 'FOUR_SHARING',  5500.00,  2),

-- PG 4: Kondapur Heights
  (9,  4, 'SINGLE',        7000.00,  5),
  (10, 4, 'DOUBLE',        5000.00,  8),
  (11, 4, 'TRIPLE',        4000.00,  6),

-- PG 5: Gachibowli Stay
  (12, 5, 'DOUBLE',        4500.00,  10),
  (13, 5, 'TRIPLE',        3500.00,   8),
  (14, 5, 'FOUR_SHARING',  3000.00,   4),

-- PG 6: Madhapur Comfort PG
  (15, 6, 'SINGLE',       10000.00,  3),
  (16, 6, 'DOUBLE',        7000.00,  5),

-- PG 7: Marine Drive PG
  (17, 7, 'SINGLE',       18000.00,  2),
  (18, 7, 'DOUBLE',       12000.00,  4),

-- PG 8: Andheri Nest
  (19, 8, 'SINGLE',       11000.00,  3),
  (20, 8, 'DOUBLE',        7500.00,  6),
  (21, 8, 'TRIPLE',        5500.00,  4),

-- PG 9: Hinjewadi Hub PG
  (22, 9, 'SINGLE',        8500.00,  4),
  (23, 9, 'DOUBLE',        6000.00,  7),

-- PG 10: Kothrud Comfort
  (24, 10, 'SINGLE',       7500.00,  3),
  (25, 10, 'DOUBLE',       5500.00,  5),
  (26, 10, 'TRIPLE',       4500.00,  3);
ALTER TABLE sharing_options ALTER COLUMN id RESTART WITH 100;

-- ── PG ↔ Facility Mappings ──────────────────────────────────
-- Use sub-selects to resolve facility IDs by name

-- PG 1: Green Nest PG → WiFi, Meals, Geyser, Water Purifier, CCTV
MERGE INTO pg_facilities (id, pg_id, facility_id) KEY (id) VALUES
  (1,  1, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (2,  1, (SELECT id FROM facilities WHERE facility_name = 'Meals')),
  (3,  1, (SELECT id FROM facilities WHERE facility_name = 'Geyser')),
  (4,  1, (SELECT id FROM facilities WHERE facility_name = 'Water Purifier')),
  (5,  1, (SELECT id FROM facilities WHERE facility_name = 'CCTV')),

-- PG 2: Sunshine Living → WiFi, AC, Gym, Parking, Power Backup, Lift
  (6,  2, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (7,  2, (SELECT id FROM facilities WHERE facility_name = 'AC')),
  (8,  2, (SELECT id FROM facilities WHERE facility_name = 'Gym')),
  (9,  2, (SELECT id FROM facilities WHERE facility_name = 'Parking')),
  (10, 2, (SELECT id FROM facilities WHERE facility_name = 'Power Backup')),
  (11, 2, (SELECT id FROM facilities WHERE facility_name = 'Lift')),

-- PG 3: Cloud9 Residency → WiFi, AC, Parking, Laundry, Housekeeping, CCTV
  (12, 3, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (13, 3, (SELECT id FROM facilities WHERE facility_name = 'AC')),
  (14, 3, (SELECT id FROM facilities WHERE facility_name = 'Parking')),
  (15, 3, (SELECT id FROM facilities WHERE facility_name = 'Laundry')),
  (16, 3, (SELECT id FROM facilities WHERE facility_name = 'Housekeeping')),
  (17, 3, (SELECT id FROM facilities WHERE facility_name = 'CCTV')),

-- PG 4: Kondapur Heights → WiFi, Meals, Laundry, Power Backup
  (18, 4, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (19, 4, (SELECT id FROM facilities WHERE facility_name = 'Meals')),
  (20, 4, (SELECT id FROM facilities WHERE facility_name = 'Laundry')),
  (21, 4, (SELECT id FROM facilities WHERE facility_name = 'Power Backup')),

-- PG 5: Gachibowli Stay → WiFi, Power Backup, Geyser
  (22, 5, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (23, 5, (SELECT id FROM facilities WHERE facility_name = 'Power Backup')),
  (24, 5, (SELECT id FROM facilities WHERE facility_name = 'Geyser')),

-- PG 6: Madhapur Comfort PG → WiFi, AC, Housekeeping, TV, Fridge
  (25, 6, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (26, 6, (SELECT id FROM facilities WHERE facility_name = 'AC')),
  (27, 6, (SELECT id FROM facilities WHERE facility_name = 'Housekeeping')),
  (28, 6, (SELECT id FROM facilities WHERE facility_name = 'TV')),
  (29, 6, (SELECT id FROM facilities WHERE facility_name = 'Fridge')),

-- PG 7: Marine Drive PG → WiFi, AC, Meals, TV, Lift, CCTV
  (30, 7, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (31, 7, (SELECT id FROM facilities WHERE facility_name = 'AC')),
  (32, 7, (SELECT id FROM facilities WHERE facility_name = 'Meals')),
  (33, 7, (SELECT id FROM facilities WHERE facility_name = 'TV')),
  (34, 7, (SELECT id FROM facilities WHERE facility_name = 'Lift')),
  (35, 7, (SELECT id FROM facilities WHERE facility_name = 'CCTV')),

-- PG 8: Andheri Nest → WiFi, Meals, Laundry, Geyser
  (36, 8, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (37, 8, (SELECT id FROM facilities WHERE facility_name = 'Meals')),
  (38, 8, (SELECT id FROM facilities WHERE facility_name = 'Laundry')),
  (39, 8, (SELECT id FROM facilities WHERE facility_name = 'Geyser')),

-- PG 9: Hinjewadi Hub PG → WiFi, AC, Gym, Parking, Power Backup
  (40, 9, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (41, 9, (SELECT id FROM facilities WHERE facility_name = 'AC')),
  (42, 9, (SELECT id FROM facilities WHERE facility_name = 'Gym')),
  (43, 9, (SELECT id FROM facilities WHERE facility_name = 'Parking')),
  (44, 9, (SELECT id FROM facilities WHERE facility_name = 'Power Backup')),

-- PG 10: Kothrud Comfort → WiFi, Meals, Geyser, Water Purifier
  (45, 10, (SELECT id FROM facilities WHERE facility_name = 'WiFi')),
  (46, 10, (SELECT id FROM facilities WHERE facility_name = 'Meals')),
  (47, 10, (SELECT id FROM facilities WHERE facility_name = 'Geyser')),
  (48, 10, (SELECT id FROM facilities WHERE facility_name = 'Water Purifier'));
ALTER TABLE pg_facilities ALTER COLUMN id RESTART WITH 100;

-- ── Images (placeholder URLs) ───────────────────────────────
MERGE INTO images (id, pg_id, image_url, display_order) KEY (id) VALUES
  (1,  1, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600', 1),
  (2,  1, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600', 2),
  (3,  2, 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600', 1),
  (4,  2, 'https://images.unsplash.com/photo-1484154218962-a197022b5858?w=600', 2),
  (5,  3, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=600', 1),
  (6,  4, 'https://images.unsplash.com/photo-1536376072261-38c75010e6c9?w=600', 1),
  (7,  4, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600', 2),
  (8,  5, 'https://images.unsplash.com/photo-1585128792020-803d29415281?w=600', 1),
  (9,  6, 'https://images.unsplash.com/photo-1560185893-a55cbc8c57e8?w=600', 1),
  (10, 7, 'https://images.unsplash.com/photo-1567767292278-a4f21aa2d36e?w=600', 1),
  (11, 8, 'https://images.unsplash.com/photo-1507089947368-19c1da9775ae?w=600', 1),
  (12, 9, 'https://images.unsplash.com/photo-1554995207-c18c203602cb?w=600', 1),
  (13, 10,'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600', 1);
ALTER TABLE images ALTER COLUMN id RESTART WITH 100;

-- ── Tenant Users ────────────────────────────────────────────────
MERGE INTO users (id, full_name, email, phone_number) KEY (email) VALUES
  (1, 'Aarav Patel',    'aarav.patel@example.com',    '9900112233'),
  (2, 'Meera Iyer',     'meera.iyer@example.com',     '9900112234'),
  (3, 'Rohit Verma',    'rohit.verma@example.com',    '9900112235'),
  (4, 'Sneha Kapoor',   'sneha.kapoor@example.com',   '9900112236'),
  (5, 'Aditya Joshi',   'aditya.joshi@example.com',   '9900112237');
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;

-- ── Preferences ─────────────────────────────────────────────
MERGE INTO preferences (id, food_preference, smoking_preference, preferred_gender, additional_notes) KEY (id) VALUES
  (1, 'VEG',     'NO_SMOKING',      'MALE',   'Early riser, prefer quiet environment'),
  (2, 'BOTH',    'NO_SMOKING',      'FEMALE', 'Working professional, need good WiFi'),
  (3, 'NON_VEG', 'SMOKING_ALLOWED', 'ANY',    'Night owl, flexible schedule'),
  (4, 'VEG',     'NO_SMOKING',      'FEMALE', 'Student, budget conscious'),
  (5, 'BOTH',    'NO_SMOKING',      'MALE',   'IT professional, need AC and parking');
ALTER TABLE preferences ALTER COLUMN id RESTART WITH 100;

-- ── Tenant Posts (Room Available posts) ──────────────────
MERGE INTO tenant_posts (id, user_id, preference_id, title, description,
    min_budget, max_budget, preferred_latitude, preferred_longitude,
    location_label, full_address, short_address, address_city, address_state, pincode,
    move_in_date, total_capacity, current_occupancy, is_active, created_at, updated_at) KEY (id) VALUES
  (1, 1, 1, '2BHK in Koramangala — 1 spot open',
       'We are 2 working professionals looking for a 3rd roommate. Clean and quiet flat with home-cooked meals available nearby.',
       7000.00, 10000.00, 12.9352, 77.6245,
       'Koramangala, Bangalore (560034)',
       'Koramangala 4th Block, Koramangala, Bangalore, Karnataka 560034, India',
       'Koramangala, Bangalore', 'Bangalore', 'Karnataka', '560034',
       '2026-05-01', 3, 2, TRUE, NOW(), NOW()),
  (2, 2, 2, 'Shared flat in Madhapur — 2 spots available',
       'Spacious 3BHK near TCS Madhapur. WiFi, laundry, and maid service included. Girls only.',
       4000.00, 7000.00, 17.4484, 78.3908,
       'Madhapur, Hyderabad (500081)',
       'Madhapur, Hyderabad, Telangana 500081, India',
       'Madhapur, Hyderabad', 'Hyderabad', 'Telangana', '500081',
       '2026-05-15', 4, 2, TRUE, NOW(), NOW()),
  (3, 3, 3, 'Room near Andheri station — looking for 1 roommate',
       'Freelancer-friendly flat with flexible hours. Good metro connectivity. Smoking allowed.',
       5000.00, 8000.00, 19.1197, 72.8464,
       'Andheri West, Mumbai (400058)',
       'Andheri West, Mumbai, Maharashtra 400058, India',
       'Andheri West, Mumbai', 'Mumbai', 'Maharashtra', '400058',
       '2026-04-20', 2, 1, TRUE, NOW(), NOW()),
  (4, 4, 4, 'Girls flat in Hinjewadi — 3 spots open',
       'Safe girls-only 4BHK near Hinjewadi IT Park with vegetarian kitchen. CCTV and security.',
       4000.00, 6500.00, 18.5912, 73.7390,
       'Hinjewadi, Pune (411057)',
       'Hinjewadi, Pune, Maharashtra 411057, India',
       'Hinjewadi, Pune', 'Pune', 'Maharashtra', '411057',
       '2026-06-01', 4, 1, TRUE, NOW(), NOW()),
  (5, 5, 5, 'AC room in HSR Layout — 1 spot available',
       'Premium 2BHK with AC, parking, and high-speed internet. Looking for a working professional.',
       10000.00, 15000.00, 12.9116, 77.6389,
       'HSR Layout, Bangalore (560102)',
       'HSR Layout, Bangalore, Karnataka 560102, India',
       'HSR Layout, Bangalore', 'Bangalore', 'Karnataka', '560102',
       '2026-05-10', 2, 1, TRUE, NOW(), NOW());
ALTER TABLE tenant_posts ALTER COLUMN id RESTART WITH 100;

-- ── Demo App User (for login/auth testing) ──────────────────
-- Password: 'password123' → BCrypt hash
MERGE INTO app_users (id, full_name, email, password, phone_number, role, created_at, updated_at) KEY (email) VALUES
  (1, 'Demo Owner',  'owner@staymatch.dev',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       '9999999901', 'PG_OWNER',  NOW(), NOW()),
  (2, 'Demo Tenant', 'tenant@staymatch.dev',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       '9999999902', 'TENANT',    NOW(), NOW()),
  (3, 'Demo Admin',  'admin@staymatch.dev',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       '9999999903', 'ADMIN',     NOW(), NOW());
ALTER TABLE app_users ALTER COLUMN id RESTART WITH 100;

