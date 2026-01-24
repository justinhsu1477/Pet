#!/bin/bash

echo "🚀 啟動 Pet Care Admin UI"
echo "=========================="
echo ""
echo "請確保後端服務已啟動在 http://localhost:8080"
echo ""
echo "選擇啟動方式:"
echo "1) Python 3 (推薦)"
echo "2) Node.js http-server"
echo "3) 直接用瀏覽器開啟 (可能有 CORS 問題)"
echo ""
read -p "請選擇 (1-3): " choice

case $choice in
    1)
        echo ""
        echo "使用 Python 3 啟動..."
        echo "訪問: http://localhost:8000"
        echo "按 Ctrl+C 停止"
        echo ""
        python3 -m http.server 8000
        ;;
    2)
        echo ""
        echo "使用 Node.js http-server 啟動..."
        echo "訪問: http://localhost:8000"
        echo "按 Ctrl+C 停止"
        echo ""
        npx http-server -p 8000
        ;;
    3)
        echo ""
        echo "使用預設瀏覽器開啟..."
        open index.html
        ;;
    *)
        echo "無效選擇"
        exit 1
        ;;
esac
