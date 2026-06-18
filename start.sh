#!/bin/bash

cd "$(dirname "$0")"

echo "=== 第三方工业设备维保工单资产台账系统 ==="
echo ""

if [ ! -f .env ]; then
    echo "❌ .env 文件不存在，请先创建"
    exit 1
fi

source .env

echo "1/3 端口冲突检测..."
./check-ports.sh
if [ $? -ne 0 ]; then
    exit 1
fi

echo "2/3 构建 Docker 镜像并启动容器..."
docker compose up --build -d
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Docker 构建启动失败"
    exit 1
fi

echo ""
echo "3/3 等待服务就绪..."
for i in $(seq 1 30); do
    if curl -sS -o /dev/null http://127.0.0.1:${FRONTEND_PORT} 2>/dev/null; then
        break
    fi
    sleep 2
done

echo ""
echo "========================================="
echo "✅ 构建启动完成！"
echo ""
echo "前端访问地址: http://localhost:${FRONTEND_PORT}"
echo "后端 API:     http://localhost:${BACKEND_PORT}/api"
echo "MySQL:        127.0.0.1:${MYSQL_PORT}"
echo "Redis:        127.0.0.1:${REDIS_PORT}"
echo "========================================="
