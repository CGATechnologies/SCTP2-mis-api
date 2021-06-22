DROP PROCEDURE IF EXISTS getRolePermissions;

CREATE PROCEDURE getRolePermissions (in role_name varchar(30))
BEGIN
	select p.*
	from permissions p
	JOIN role_permissions rp ON rp.permission = p.name
	WHERE rp.`role` = role_name;
END