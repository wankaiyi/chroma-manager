#!/bin/bash
# 适用于 Linux / MacOS

set -e
set -o pipefail

echo "========================================"
echo "   chroma manager 正在启动……"
echo "========================================"

SPRING_BOOT_PATH="./chroma-manager-backend"
VUE_PATH="./chroma-manager-fronted"

BACKEND_PORT=8011
FRONTEND_PORT=8012

if [ ! -d "$SPRING_BOOT_PATH" ]; then
  echo "[错误] Spring Boot 项目目录不存在: $SPRING_BOOT_PATH"
  exit 1
fi

if [ ! -d "$VUE_PATH" ]; then
  echo "[错误] Vue 项目目录不存在: $VUE_PATH"
  exit 1
fi

echo "chroma-manager-backend 端口: $BACKEND_PORT"
echo "chroma-manager-fronted 端口: $FRONTEND_PORT"
echo

# -------------------------------
# 启动 Spring Boot 后端服务
# -------------------------------
echo "[1/2] 启动 chroma-manager-backend 后端服务..."
cd "$SPRING_BOOT_PATH"

# Mac 和 Linux 不同的终端启动方式
if [[ "$OSTYPE" == "darwin"* ]]; then
  # MacOS
  osascript -e "tell app \"Terminal\" to do script \"cd $(pwd) && mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=$BACKEND_PORT'\""
else
  # Linux (GNOME Terminal / xterm 兼容)
  gnome-terminal -- bash -c "echo Starting Spring Boot on port $BACKEND_PORT...; mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=$BACKEND_PORT'; exec bash" \
    || xterm -hold -e "cd $(pwd) && echo Starting Spring Boot on port $BACKEND_PORT... && mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=$BACKEND_PORT'"
fi

sleep 3
cd - > /dev/null

# -------------------------------
# 启动 Vue 前端
# -------------------------------
echo "[2/2] 启动 Vue 前端服务..."
cd "$VUE_PATH"

# 写入新的 .env
cat > .env <<EOF
VUE_APP_CHROMA_API_URL=http://localhost:$BACKEND_PORT
VUE_APP_TITLE=ChromaDB Manager
EOF

if [[ "$OSTYPE" == "darwin"* ]]; then
  osascript -e "tell app \"Terminal\" to do script \"cd $(pwd) && npm install && npm run serve -- --port $FRONTEND_PORT\""
else
  gnome-terminal -- bash -c "echo Starting Vue on port $FRONTEND_PORT...; npm install && npm run serve -- --port $FRONTEND_PORT; exec bash" \
    || xterm -hold -e "cd $(pwd) && echo Starting Vue on port $FRONTEND_PORT... && npm install && npm run serve -- --port $FRONTEND_PORT"
fi

cd - > /dev/null

echo
echo "========================================"
echo "   项目启动完成！"
echo "   chroma-manager-backend: http://localhost:$BACKEND_PORT"
echo "   chroma-manager-fronted: http://localhost:$FRONTEND_PORT"
echo "========================================"
echo
