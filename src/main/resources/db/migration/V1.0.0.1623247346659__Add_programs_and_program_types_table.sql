CREATE TABLE IF NOT EXISTS program_types (
    id bigint primary key,
    name varchar(100) not null,
    created_at timestamp not null,
    active boolean not null
);

CREATE TABLE IF NOT EXISTS programs (
    id bigint primary key,
    code varchar(10) not null unique comment 'Program ID / PG-XXXX',
    created_at timestamp not null,
    start_date datetime not null,
    end_date datetime,
    program_type bigint not null,
    delivery_agency bigint not null,
    active boolean not null,
    foreign key (program_type) references program_types (id),
    foreign key (delivery_agency) references delivery_agencies (id)
);

INSERT INTO permission_groups(name, description) VALUES ('targeting', 'Targeting');

INSERT INTO permissions (name, description, `group`, system_permission, active)
VALUES
    ('READ_PROGRAMS', 'Access programs and program types.', 'targeting', 0, 1)
   ,('WRITE_PROGRAMS', 'Add and modify programs and program types.', 'targeting', 0, 1)
;