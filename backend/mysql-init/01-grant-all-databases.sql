-- The official MySQL image only grants MYSQL_USER privileges on the single
-- MYSQL_DATABASE it bootstraps. Since auth-service, tenant-service, and
-- project-service each connect with the same admin user but a different
-- database name (createDatabaseIfNotExist=true in their JDBC URLs), that
-- user needs CREATE + full privileges across all databases it might create.
--
-- This is a local-dev / demo convenience. For production, prefer either a
-- dedicated least-privilege user per service, or separate database
-- instances per service entirely.
GRANT ALL PRIVILEGES ON *.* TO 'mt_admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
