#!/bin/bash
# MSSQL Database Initialization Script
# Run this ONCE after first docker-compose up

echo "Waiting for MSSQL to be ready..."
until docker exec pet-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "Passw0rd" -Q "SELECT 1" &> /dev/null
do
  echo "MSSQL is starting up..."
  sleep 3
done

echo "MSSQL is ready! Creating database..."

# Create database
docker exec pet-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "Passw0rd" -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'petdb')
BEGIN
    CREATE DATABASE petdb;
    PRINT 'Database petdb created successfully';
END
ELSE
BEGIN
    PRINT 'Database petdb already exists';
END
"

echo "Database initialization complete!"
echo ""
#echo "Next step: Run schema-mssql.sql to create tables"
#echo "  docker exec -i pet-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Passw0rd' -d petdb < src/main/resources/db/schema-mssql.sql"
echo "Importing schema..."
docker exec -i pet-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'Passw0rd' -d petdb < src/main/resources/db/schema-mssql.sql

echo ""
echo "========================================="
echo "All Done! Your database is ready to use."
echo "========================================="
