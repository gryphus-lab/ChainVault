-- setup.sql
CREATE SCHEMA chainvault AUTHORIZATION chainvault;
CREATE SCHEMA flowable AUTHORIZATION chainvault;
GRANT ALL PRIVILEGES ON SCHEMA chainvault TO chainvault;
GRANT ALL PRIVILEGES ON SCHEMA flowable TO chainvault;