-- Initialize databases for WHO Cloud services
-- Create separate databases for different services

-- Auth Service Database
CREATE DATABASE authdb;

-- User Management Service Database
CREATE DATABASE userdb;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE authdb TO whocloud;
GRANT ALL PRIVILEGES ON DATABASE userdb TO whocloud;
