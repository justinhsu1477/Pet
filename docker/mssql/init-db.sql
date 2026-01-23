-- MSSQL Database Initialization
-- This script creates the required databases
-- Schema and data will be managed by Hibernate and Spring Boot

-- Create Primary database
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

-- Create Log database
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