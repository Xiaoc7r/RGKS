@echo off
chcp 65001 >nul
echo 正在停止高校选课注册系统...
taskkill /FI "WINDOWTITLE eq RGKS Backend*" /T /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq RGKS Frontend*" /T /F >nul 2>nul
echo 已停止本项目的前端和后端窗口。
ping 127.0.0.1 -n 3 >nul
