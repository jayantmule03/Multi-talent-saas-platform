CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS tenant_db;
CREATE DATABASE IF NOT EXISTS project_db;

GRANT ALL PRIVILEGES ON auth_db.* TO 'mt_admin'@'%';
GRANT ALL PRIVILEGES ON tenant_db.* TO 'mt_admin'@'%';
GRANT ALL PRIVILEGES ON project_db.* TO 'mt_admin'@'%';

FLUSH PRIVILEGES;