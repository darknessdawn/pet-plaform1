@echo off
chcp 65001
echo 正在启动宠物用品交易平台后端服务...
echo.

REM 检查Maven是否已下载
if not exist "apache-maven-3.9.5" (
    echo 正在下载Maven...
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip' -OutFile 'maven.zip'"
    echo 正在解压Maven...
    powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.'"
    del maven.zip
)

REM 设置Maven环境变量
set "MAVEN_HOME=%CD%\apache-maven-3.9.5"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Maven版本:
call mvn -v
echo.

REM 编译并启动项目
echo 正在编译并启动项目...
call mvn clean spring-boot:run

pause
