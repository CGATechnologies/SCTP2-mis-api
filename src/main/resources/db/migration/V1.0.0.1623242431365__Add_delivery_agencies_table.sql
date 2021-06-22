CREATE TABLE IF NOT EXISTS delivery_agencies (
    id bigint primary key,
    name varchar(100) not null,
    locationId bigint not null,
    created_at timestamp not null,
    active boolean not null,
    foreign key(locationId) references locations (id)
);

INSERT INTO permission_groups (name, description) VALUES ('transfers', 'Transfers');

INSERT INTO permissions (name, description, `group`, system_permission, active)
VALUES
    ('READ_DELIVERY_AGENCIES', 'Access delivery agencies.', 'transfers', 0, 1)
   ,('WRITE_DELIVERY_AGENCIES', 'Add and modify delivery agencies.', 'transfers', 0, 1)
;