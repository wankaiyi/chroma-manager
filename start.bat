@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo    chroma manager 正在启动……
echo ========================================

set SPRING_BOOT_PATH=.\chroma-manager-backend
set VUE_PATH=.\chroma-manager-fronted

set BACKEND_PORT=8011
set FRONTED_PORT=8012

if not exist "%SPRING_BOOT_PATH%" (
    echo [错误] Spring Boot项目目录不存在: %SPRING_BOOT_PATH%
    pause
    exit /b 1
)

if not exist "%VUE_PATH%" (
    echo [错误] Vue项目目录不存在: %VUE_PATH%
    pause
    exit /b 1
)

echo chroma-manager-backend 端口: %BACKEND_PORT%
echo chroma-manager-fronted 端口: %FRONTED_PORT%
echo.

echo [1/2] 启动chroma-manager-backend后端服务...
cd /d "%SPRING_BOOT_PATH%"
start /b "Spring Boot Server" cmd /k "echo Starting Spring Boot on port %BACKEND_PORT%... && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=%BACKEND_PORT%"

timeout /t 3 /nobreak > nul

echo [2/2] 启动Vue前端服务...
cd /d "..\%VUE_PATH%"

(
echo VUE_APP_CHROMA_API_URL=http://localhost:%BACKEND_PORT%
echo VUE_APP_TITLE=ChromaDB Manager
) > .env
start /b "Vue Dev Server" cmd /k "echo Starting Vue on port %FRONTED_PORT%... && npm i && npm run serve -- --port %FRONTED_PORT%"

echo.
echo ========================================
echo    项目启动完成！
echo    chroma-manager-backend: http://localhost:%BACKEND_PORT%
echo    chroma-manager-fronted: http://localhost:%FRONTED_PORT%
echo ========================================
echo.

cd /d %~dp0

pause