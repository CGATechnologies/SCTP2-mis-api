CREATE TABLE IF NOT EXISTS `locations` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `code` varchar(30) UNIQUE NOT NULL COMMENT 'Human readable location code LXXXX',
  `name` varchar(100) NOT NULL,
  `parent_id` bigint,
  `location_type` varchar(50) NOT NULL COMMENT 'country, subnational1, subnational2, subnational3, subnational4',
  `created_at` timestamp NOT NULL,
  `latitude` decimal(6, 3), -- ±90.000° @ 100 meters
  `longitude` decimal(7, 3), -- ±180.000° @ 100 meters
  `active` boolean NOT NULL
);

CREATE OR REPLACE VIEW `active_locations`
    AS SELECT * FROM locations WHERE active = true;