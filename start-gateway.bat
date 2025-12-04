@echo off
REM ========================================
REM Gateway Service 启动脚本
REM ========================================

chcp 65001 > nul

setlocal

echo ========================================
echo 正在启动 Gateway Service，端口为 8080
echo ========================================
echo.

cd /d "%~dp0gateway-service"
call ..\mvnw.cmd spring-boot:run

pause

