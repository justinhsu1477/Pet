#!/bin/bash

# Quick database test script

echo "Testing MSSQL connection..."

docker-compose -f docker-compose.qas.yml exec -T mssql /opt/mssql-tools/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "Passw0rd" \
  -Q "SELECT name FROM sys.databases WHERE name IN ('petdb', 'petdb_log')"

echo ""
echo "Testing petdb tables..."

docker-compose -f docker-compose.qas.yml exec -T mssql /opt/mssql-tools/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "Passw0rd" \
  -d petdb \
  -Q "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
