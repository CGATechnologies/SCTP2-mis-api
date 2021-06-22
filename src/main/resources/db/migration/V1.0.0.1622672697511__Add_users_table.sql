CREATE TABLE IF NOT EXISTS `status_codes` (
    id int primary key auto_increment,
    status varchar(100) not null,
    code int not null unique
);

CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(30) UNIQUE NOT NULL,
  `description` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL,
  `active` boolean NOT NULL,
  `is_system_role` boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS permission_groups (
   `id` bigint PRIMARY KEY AUTO_INCREMENT,
   `name` varchar(30) UNIQUE NOT NULL,
   `description` varchar(100) not null
);

CREATE TABLE IF NOT EXISTS `permissions` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(30) UNIQUE NOT NULL,
  `description` varchar(100) NOT NULL,
  `group` varchar(30) NOT NULL,
  system_permission boolean not null,
  `active` boolean NOT NULL,

  FOREIGN KEY(`group`) REFERENCES permission_groups(name)
);

CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `created_at` timestamp NOT NULL,
  `location` bigint NOT NULL,
  `modified_at` timestamp,
  `last_auth_attempt_at` timestamp,
  `session_id` varchar(512),
  `ip_address` varchar(20),
  `status` INT NOT NULL,
  `status_text` text,
  `auth_attempts` INT NOT NULL,
  `user_name` varchar(20) UNIQUE NOT NULL,
  `email` varchar(50) UNIQUE NOT NULL,
  `password` varchar(512) NOT NULL,
  `role` varchar(30) NOT NULL,
  `system_user` boolean not null,

  FOREIGN KEY(location) REFERENCES locations(id),
  FOREIGN KEY(role) REFERENCES roles(name)
);

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `role` varchar(30) NOT NULL,
  `permission` varchar(30) NOT NULL,

  FOREIGN KEY(role) REFERENCES roles(name),
  FOREIGN KEY(permission) REFERENCES permissions (name)
);

insert into status_codes (code, status)
values
    (0, 'Inactive')
  , (1, 'Active')
  , (2, 'Deleted')
;

INSERT INTO `roles` (name, description, created_at, is_system_role, active)
VALUES
    ('ROLE_SYSTEM_ADMIN', 'Super System Administrator', CURRENT_TIMESTAMP, true, true)
;

INSERT into permission_groups (name, description)
    VALUES ('user_management','User Management')
;

INSERT INTO `permissions` (name, description, `group`, active, system_permission)
VALUES
    ('READ_USERS', 'Access users', 'user_management', true, false)
   ,('WRITE_USERS', 'Modify users', 'user_management', true, false)
;

INSERT INTO `role_permissions` (`role`, `permission`)
VALUES
    ('ROLE_SYSTEM_ADMIN', 'READ_USERS')
   ,('ROLE_SYSTEM_ADMIN', 'WRITE_USERS')
;

CREATE OR REPLACE VIEW `active_users` AS
    SELECT * FROM users WHERE status = 1;