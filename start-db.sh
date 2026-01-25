#!/bin/bash
# 啟動 MSSQL 資料庫 (用於本地開發)

echo "🚀 啟動 MSSQL 資料庫..."
docker-compose -f docker-compose.db.yml up -d

echo ""
echo "⏳ 等待資料庫啟動..."
sleep 10

echo ""
echo "✅ 資料庫已啟動!"
echo ""
echo "📊 連線資訊:"
echo "   Host: localhost"
echo "   Port: 1433"
echo "   Username: sa"
echo "   Password: Passw0rd"
echo "   Database: petdb, petdb_log"
echo ""
echo "💡 在 IDE 中啟動 Spring Boot 時,使用 qas profile:"
echo "   -Dspring.profiles.active=qas"
echo ""
echo "🛑 停止資料庫: ./stop-db.sh"