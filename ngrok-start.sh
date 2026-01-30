#!/bin/bash
# ============================================
# ngrok 自動啟動 + 環境變數更新腳本
# 用途：啟動 ngrok 並自動更新 .env 和 LINE Webhook
# ============================================

set -e

# 顏色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

# 預設值
PORT=${1:-3000}
ENV_FILE=".env"

echo -e "${CYAN}🚀 啟動 ngrok (port: $PORT)...${NC}"

# 檢查 ngrok 是否安裝
if ! command -v ngrok &> /dev/null; then
    echo -e "${RED}❌ ngrok 未安裝，請執行: brew install ngrok${NC}"
    exit 1
fi

# 如果已有 ngrok 在跑，先停掉
if pgrep -x "ngrok" > /dev/null; then
    echo -e "${YELLOW}⚠️  偵測到 ngrok 正在執行，先停止...${NC}"
    killall ngrok 2>/dev/null || true
    sleep 1
fi

# 背景啟動 ngrok
ngrok http $PORT > /dev/null 2>&1 &
NGROK_PID=$!
echo -e "${GREEN}✅ ngrok 已啟動 (PID: $NGROK_PID)${NC}"

# 等待 ngrok 準備好
echo -n "⏳ 等待 ngrok 產生 URL"
for i in {1..15}; do
    sleep 1
    echo -n "."
    NGROK_URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    for t in data.get('tunnels', []):
        if t.get('proto') == 'https':
            print(t['public_url'])
            break
except:
    pass
" 2>/dev/null)
    if [ -n "$NGROK_URL" ]; then
        break
    fi
done
echo ""

if [ -z "$NGROK_URL" ]; then
    echo -e "${RED}❌ 無法取得 ngrok URL，請檢查 ngrok 狀態${NC}"
    echo -e "${YELLOW}💡 嘗試: curl http://localhost:4040/api/tunnels${NC}"
    exit 1
fi

echo -e "${GREEN}🌐 ngrok URL: ${CYAN}$NGROK_URL${NC}"
echo ""

# ============================================
# 更新 .env 檔案
# ============================================
update_env_file() {
    local file=$1
    if [ ! -f "$file" ]; then
        return
    fi

    echo -e "${YELLOW}📝 更新 $file ...${NC}"

    # 更新 LINE_BASE_URL
    if grep -q "^LINE_BASE_URL=" "$file"; then
        sed -i '' "s|^LINE_BASE_URL=.*|LINE_BASE_URL=$NGROK_URL|" "$file"
        echo -e "   LINE_BASE_URL=${CYAN}$NGROK_URL${NC}"
    fi

    # 更新 LINE_FRONTEND_URL
    if grep -q "^LINE_FRONTEND_URL=" "$file"; then
        sed -i '' "s|^LINE_FRONTEND_URL=.*|LINE_FRONTEND_URL=$NGROK_URL|" "$file"
        echo -e "   LINE_FRONTEND_URL=${CYAN}$NGROK_URL${NC}"
    fi

    # 更新 ALLOWED_ORIGINS（加入 ngrok URL）
    if grep -q "^ALLOWED_ORIGINS=" "$file"; then
        CURRENT_ORIGINS=$(grep "^ALLOWED_ORIGINS=" "$file" | cut -d'=' -f2-)
        # 先移除舊的 ngrok URL
        CLEANED=$(echo "$CURRENT_ORIGINS" | sed 's|,https://[^,]*\.ngrok-free\.[^,]*||g' | sed 's|https://[^,]*\.ngrok-free\.[^,]*,||g' | sed 's|https://[^,]*\.ngrok-free\.[^,]*||g')
        NEW_ORIGINS="${NGROK_URL},${CLEANED}"
        sed -i '' "s|^ALLOWED_ORIGINS=.*|ALLOWED_ORIGINS=$NEW_ORIGINS|" "$file"
        echo -e "   ALLOWED_ORIGINS=${CYAN}$NEW_ORIGINS${NC}"
    fi

    # 更新 CORS_ORIGINS（Spring Boot CORS 設定，加入 ngrok URL）
    if grep -q "^CORS_ORIGINS=" "$file"; then
        CURRENT_CORS=$(grep "^CORS_ORIGINS=" "$file" | cut -d'=' -f2-)
        # 先移除舊的 ngrok URL
        CLEANED_CORS=$(echo "$CURRENT_CORS" | sed 's|,https://[^,]*\.ngrok-free\.[^,]*||g' | sed 's|https://[^,]*\.ngrok-free\.[^,]*,||g' | sed 's|https://[^,]*\.ngrok-free\.[^,]*||g')
        if [ -z "$CLEANED_CORS" ]; then
            NEW_CORS="http://localhost:3000,http://localhost:8080,${NGROK_URL}"
        else
            NEW_CORS="${CLEANED_CORS},${NGROK_URL}"
        fi
        sed -i '' "s|^CORS_ORIGINS=.*|CORS_ORIGINS=$NEW_CORS|" "$file"
        echo -e "   CORS_ORIGINS=${CYAN}$NEW_CORS${NC}"
    else
        # CORS_ORIGINS 不存在則新增
        echo "CORS_ORIGINS=http://localhost:3000,http://localhost:8080,${NGROK_URL}" >> "$file"
        echo -e "   CORS_ORIGINS=${CYAN}http://localhost:3000,http://localhost:8080,${NGROK_URL}${NC} (新增)"
    fi

    echo -e "${GREEN}   ✅ $file 更新完成${NC}"
}

# 更新所有 .env 檔
update_env_file ".env"
update_env_file ".env.dev"
update_env_file ".env.qas"

echo ""

# ============================================
# 更新 LINE Messaging API Webhook URL
# ============================================
update_line_webhook() {
    # 從 .env 讀取 LINE Channel Token
    if [ -f ".env" ]; then
        LINE_TOKEN=$(grep "^LINE_CHANNEL_TOKEN=" .env | cut -d'=' -f2-)
    fi

    if [ -z "$LINE_TOKEN" ] || [ "$LINE_TOKEN" = "your_channel_access_token_here" ]; then
        echo -e "${YELLOW}⚠️  LINE_CHANNEL_TOKEN 未設定，跳過 Webhook 更新${NC}"
        return
    fi

    WEBHOOK_URL="${NGROK_URL}/api/line/webhook"
    echo -e "${YELLOW}📡 更新 LINE Webhook URL...${NC}"
    echo -e "   URL: ${CYAN}$WEBHOOK_URL${NC}"

    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT \
        https://api.line.me/v2/bot/channel/webhook/endpoint \
        -H "Authorization: Bearer $LINE_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"endpoint\": \"$WEBHOOK_URL\"}")

    if [ "$RESPONSE" = "200" ]; then
        echo -e "${GREEN}   ✅ LINE Webhook 更新成功！${NC}"
    else
        echo -e "${RED}   ❌ LINE Webhook 更新失敗 (HTTP $RESPONSE)${NC}"
        echo -e "${YELLOW}   💡 請手動到 LINE Developers Console 設定${NC}"
    fi
}

update_line_webhook

# ============================================
# 重建 LINE Rich Menu（更新選單內的 URL）
# ============================================
recreate_rich_menu() {
    echo -e "${YELLOW}🎨 重建 LINE Rich Menu...${NC}"

    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
        "http://localhost:8080/api/line/richmenu/recreate")

    if [ "$RESPONSE" = "200" ]; then
        echo -e "${GREEN}   ✅ Rich Menu 重建成功！${NC}"
    else
        echo -e "${YELLOW}   ⚠️  Rich Menu 重建跳過 (後端可能未啟動，啟動後會自動建立)${NC}"
    fi
}

recreate_rich_menu

# ============================================
# 顯示摘要
# ============================================
echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}✅ 全部完成！${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "🌐 ngrok URL:    ${CYAN}$NGROK_URL${NC}"
echo -e "🖥️  前端:         ${CYAN}$NGROK_URL${NC}"
echo -e "🔌 API:          ${CYAN}$NGROK_URL/api${NC}"
echo -e "📡 Webhook:      ${CYAN}$NGROK_URL/api/line/webhook${NC}"
echo -e "🔑 LINE Login:   ${CYAN}$NGROK_URL/api/auth/oauth2/callback/line${NC}"
echo ""
echo -e "${YELLOW}⚠️  LINE Login Callback URL 需手動到 LINE Developers Console 更新：${NC}"
echo -e "   ${CYAN}$NGROK_URL/api/auth/oauth2/callback/line${NC}"
echo ""
echo -e "${YELLOW}📋 ngrok 管理介面: ${CYAN}http://127.0.0.1:4040${NC}"
echo -e "${YELLOW}🛑 停止 ngrok: ${CYAN}./ngrok-stop.sh${NC} 或 ${CYAN}killall ngrok${NC}"
echo ""

# 保持前台顯示 ngrok log
echo -e "${CYAN}📺 ngrok 運行中... (Ctrl+C 停止)${NC}"
wait $NGROK_PID 2>/dev/null
