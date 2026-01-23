#!/bin/bash

# 啟動 qas 環境所需的資料庫 (僅 MSSQL)
echo "🚀 Starting QAS environment (MSSQL)..."
echo ""

# 啟動 Docker 容器
docker compose --profile qas up -d

echo ""
echo "⏳ Waiting for MSSQL to be ready..."
echo ""

# 等待 MSSQL 健康檢查通過
until docker exec pet-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "Passw0rd" -Q "SELECT 1" &> /dev/null
do
  echo "   MSSQL is starting up..."
  sleep 3
done

echo ""
echo "========================================="
echo "✅ QAS Environment Ready!"
echo "========================================="
echo "📊 Primary DB: jdbc:sqlserver://localhost:1433;databaseName=petdb"
echo "📝 Log DB: jdbc:sqlserver://localhost:1433;databaseName=petdb_log"
echo "👤 Username: sa"
echo "🔑 Password: Passw0rd"
echo "========================================="
echo ""
echo "📌 Next Steps:"
echo "   1. Run: mvn spring-boot:run -Dspring-boot.run.profiles=qas"
echo "   2. Spring Boot will auto-create tables (ddl-auto: update)"
echo "   3. Test data will be loaded automatically"
echo ""
echo "🛑 To stop: docker compose --profile qas down"
echo ""
