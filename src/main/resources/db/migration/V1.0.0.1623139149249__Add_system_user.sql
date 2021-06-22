INSERT INTO locations (
	code
	,name
	,parent_id
	,location_type
	,created_at
	,latitude
	,longitude
	,active
) VALUES (
	'L00000'
	,'Malawi'
	,NULL
	,'COUNTRY'
	,CURRENT_TIMESTAMP
	,-13.947
	,33.787
	,1
) ON DUPLICATE KEY UPDATE id = id;

INSERT INTO `users`(
 first_name
,last_name
,created_at
,location
,modified_at
,`status`
,auth_attempts
,user_name
,email
,`password`
,`role`
,`system_user`
) VALUES (
	'System'
   ,'Administrator'
   ,CURRENT_TIMESTAMP
   ,1
   ,CURRENT_TIMESTAMP
   ,1
   ,0
   ,'system'
   ,'system@localhost'
   ,'$2y$12$1ozxC9F138ClHRUCqjFlhO0t/Cdc7qvtssa2CogsMQbjLTogG4UCK'
   ,'ROLE_SYSTEM_ADMIN'
   ,1
) ON DUPLICATE KEY UPDATE id = id;