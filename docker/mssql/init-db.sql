-- Create databases if they don't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'petdb')
BEGIN
    CREATE DATABASE petdb;
    PRINT 'Database petdb created successfully';
END
ELSE
BEGIN
    PRINT 'Database petdb already exists';
END
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'petdb_log')
BEGIN
    CREATE DATABASE petdb_log;
    PRINT 'Database petdb_log created successfully';
END
ELSE
BEGIN
    PRINT 'Database petdb_log already exists';
END
GO

PRINT 'Database initialization completed';
GO
