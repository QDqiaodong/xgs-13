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

find_port() {
    local port=$1
    while lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; do
        echo "端口 $port 已被占用，尝试下一个端口..." >&2
        port=$((port + 1))
    done
    echo "$port"
}

echo "=== 端口冲突检测 ==="
echo ""

FRONTEND_PORT=$(find_port "$FRONTEND_PORT")
BACKEND_PORT=$(find_port "$BACKEND_PORT")
MYSQL_PORT=$(find_port "$MYSQL_PORT")
REDIS_PORT=$(find_port "$REDIS_PORT")

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

cat > .env.runtime <<EOF
COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME}
DOCKER_REGISTRY=${DOCKER_REGISTRY}
FRONTEND_PORT=${FRONTEND_PORT}
BACKEND_PORT=${BACKEND_PORT}
MYSQL_PORT=${MYSQL_PORT}
REDIS_PORT=${REDIS_PORT}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_USER=${MYSQL_USER}
MYSQL_PASSWORD=${MYSQL_PASSWORD}
EOF
