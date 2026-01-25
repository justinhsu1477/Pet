#!/bin/bash

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to start
echo "Waiting for SQL Server to start..."
sleep 30s

# Run the database initialization script
echo "Creating databases..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "${SA_PASSWORD}" -i /docker-entrypoint-initdb.d/init-db.sql

# Check if databases were created
echo "Verifying databases..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "${SA_PASSWORD}" -Q "SELECT name FROM sys.databases WHERE name IN ('petdb', 'petdb_log')"

echo "Database initialization completed"

# Keep the container running
wait
