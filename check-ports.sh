#!/bin/bash

cd "$(dirname "$0")"

source .env

ERRORS=()

check_port() {
    local port=$1
    local name=$2
    local result
    result=$(lsof -nP -iTCP:"$port" -sTCP:LISTEN 2>/dev/null)
    if [ -n "$result" ]; then
        ERRORS+=("端口 $port ($name) 已被占用:\n$result")
    fi
}

echo "=== 端口冲突检测 ==="
echo ""

check_port "$FRONTEND_PORT" "前端"
check_port "$BACKEND_PORT" "后端"
check_port "$MYSQL_PORT" "MySQL"
check_port "$REDIS_PORT" "Redis"

if [ ${#ERRORS[@]} -gt 0 ]; then
    echo "❌ 端口冲突检测失败！"
    echo ""
    for err in "${ERRORS[@]}"; do
        echo -e "$err"
        echo ""
    done
    echo "请先停止占用上述端口的进程，或修改 .env 文件中的端口配置。"
    exit 1
fi

echo "✅ 端口检测通过"
echo "  前端: $FRONTEND_PORT"
echo "  后端: $BACKEND_PORT"
echo "  MySQL: $MYSQL_PORT"
echo "  Redis: $REDIS_PORT"
echo ""
