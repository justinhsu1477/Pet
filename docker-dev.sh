#!/bin/bash

# dev 環境使用 H2 file-based database，不需要 Docker
echo "🚀 Dev environment uses H2 file-based databases"
echo ""
echo "📊 Primary DB: jdbc:h2:file:./data/petdb"
echo "📊 Log DB: jdbc:h2:file:./data/petdb_log"
echo ""
echo "💡 H2 Console: http://localhost:8080/h2-console"
echo ""
echo "No Docker containers needed for dev!"
