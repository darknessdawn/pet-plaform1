@echo off
chcp 65001
echo 正在启动宠物用品交易平台前端服务...
echo.

cd pet-platform-frontend

REM 检查node_modules是否存在
if not exist "node_modules" (
    echo 正在安装前端依赖...
    call npm install
)

echo 正在启动前端开发服务器...
call npm run dev

pause
