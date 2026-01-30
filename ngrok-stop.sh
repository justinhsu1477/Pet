#!/bin/bash
# ============================================
# ngrok 停止 + 還原 .env 腳本
# ============================================

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${YELLOW}🛑 停止 ngrok...${NC}"

if pgrep -x "ngrok" > /dev/null; then
    killall ngrok 2>/dev/null
    echo -e "${GREEN}✅ ngrok 已停止${NC}"
else
    echo -e "${YELLOW}⚠️  ngrok 未在執行${NC}"
fi

# 還原 .env 為 localhost
restore_env_file() {
    local file=$1
    local base_url=$2
    local frontend_url=$3

    if [ ! -f "$file" ]; then
        return
    fi

    echo -e "${YELLOW}📝 還原 $file ...${NC}"

    if grep -q "^LINE_BASE_URL=" "$file"; then
        sed -i '' "s|^LINE_BASE_URL=.*|LINE_BASE_URL=$base_url|" "$file"
    fi

    if grep -q "^LINE_FRONTEND_URL=" "$file"; then
        sed -i '' "s|^LINE_FRONTEND_URL=.*|LINE_FRONTEND_URL=$frontend_url|" "$file"
    fi

    # 移除 ngrok URL from ALLOWED_ORIGINS
    if grep -q "^ALLOWED_ORIGINS=" "$file"; then
        sed -i '' 's|,https://[^,]*\.ngrok-free\.[^,]*||g' "$file"
        sed -i '' 's|https://[^,]*\.ngrok-free\.[^,]*,||g' "$file"
        sed -i '' 's|https://[^,]*\.ngrok-free\.[^,]*||g' "$file"
    fi

    # 還原 CORS_ORIGINS（移除 ngrok URL）
    if grep -q "^CORS_ORIGINS=" "$file"; then
        sed -i '' 's|,https://[^,]*\.ngrok-free\.[^,]*||g' "$file"
        sed -i '' 's|https://[^,]*\.ngrok-free\.[^,]*,||g' "$file"
        sed -i '' 's|https://[^,]*\.ngrok-free\.[^,]*||g' "$file"
    fi

    echo -e "${GREEN}   ✅ $file 已還原${NC}"
}

restore_env_file ".env" "http://localhost:8080" "http://localhost:3000"
restore_env_file ".env.dev" "http://172.20.10.2:8080" "http://localhost:3000"
restore_env_file ".env.qas" "http://localhost:8080" "http://localhost"

echo ""
echo -e "${GREEN}✅ 全部還原完成！.env 已改回 localhost${NC}"
