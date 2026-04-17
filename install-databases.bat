@echo off
chcp 65001
echo ========================================
echo 正在安装 MySQL 和 Redis
echo ========================================
echo.

REM 创建数据库目录
if not exist "database" mkdir database
cd database

REM 下载并安装Redis
echo [1/2] 正在下载 Redis...
if not exist "redis" (
    mkdir redis
    cd redis
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/microsoftarchive/redis/releases/download/win-3.0.504/Redis-x64-3.0.504.zip' -OutFile 'redis.zip' -UseBasicParsing"
    echo 正在解压 Redis...
    powershell -Command "Expand-Archive -Path 'redis.zip' -DestinationPath '.' -Force"
    del redis.zip
    cd ..
)

REM 下载并安装MySQL绿色版
echo [2/2] 正在下载 MySQL...
if not exist "mysql" (
    mkdir mysql
    cd mysql
    powershell -Command "Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.35-winx64.zip' -OutFile 'mysql.zip' -UseBasicParsing"
    echo 正在解压 MySQL...
    powershell -Command "Expand-Archive -Path 'mysql.zip' -DestinationPath '.' -Force"
    move mysql-8.0.35-winx64 mysql-8.0
    del mysql.zip
    
    REM 初始化MySQL
    echo 正在初始化 MySQL...
    cd mysql-8.0
    mkdir data
    bin\mysqld --initialize-insecure --console
    cd ..\..
)

echo.
echo ========================================
echo 安装完成！
echo ========================================
echo.
echo 启动命令：
echo   - 启动Redis: database\redis\redis-server.exe
echo   - 启动MySQL: database\mysql\mysql-8.0\bin\mysqld.exe --console
echo.
pause
