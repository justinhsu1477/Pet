#!/bin/bash
# 停止 MSSQL 資料庫

echo "🛑 停止 MSSQL 資料庫..."
docker-compose -f docker-compose.db.yml down

echo "✅ 資料庫已停止!"