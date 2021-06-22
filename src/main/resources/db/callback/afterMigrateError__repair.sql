--
-- Callback SQL file to automatically remove failed migrations due to MySQL not supporting DDL transactions.
--
-- This will allow us to revise the migration scripts.
--
-- Professional advice? Use PostgreSQL.
--
DELETE FROM flyway_schema_history WHERE success = false;