@echo off
chcp 65001 >nul
set "PROJECT_ROOT=%~dp0"

echo ========================================
echo   高校选课注册系统 - 答辩演示模式
echo ========================================
echo.

where java >nul 2>nul
if errorlevel 1 goto :missing_java
where mvn >nul 2>nul
if errorlevel 1 goto :missing_maven
where node >nul 2>nul
if errorlevel 1 goto :missing_node
where npm >nul 2>nul
if errorlevel 1 goto :missing_node

echo [1/3] 启动后端（H2 内存数据库，自动恢复演示数据）...
start "RGKS Backend" /D "%PROJECT_ROOT%backend" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=demo"

echo [2/3] 启动前端...
start "RGKS Frontend" /D "%PROJECT_ROOT%frontend" cmd /k "call npm install && call npm run dev"

echo [3/3] 等待服务初始化，随后打开浏览器...
ping 127.0.0.1 -n 11 >nul
start "" "http://localhost:5173"

echo.
echo Startup complete. Close the project with the stop script after the defense.
ping 127.0.0.1 -n 4 >nul
exit /b 0

:missing_java
echo [错误] 未找到 Java。请安装 JDK 21，并确认 java 已加入 PATH。
pause
exit /b 1

:missing_maven
echo [错误] 未找到 Maven。请安装 Maven 3.9+，并确认 mvn 已加入 PATH。
pause
exit /b 1

:missing_node
echo [错误] 未找到 Node.js/npm。请安装 Node.js 20+，并确认 node、npm 已加入 PATH。
pause
exit /b 1
