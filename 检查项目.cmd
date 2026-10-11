@echo off
chcp 65001 >nul
set "PROJECT_ROOT=%~dp0"

echo ========================================
echo   高校选课注册系统 - 发布前检查
echo ========================================

pushd "%PROJECT_ROOT%backend"
echo [1/2] 后端 7 模块集成测试...
call mvn test
if errorlevel 1 goto :failed
popd

pushd "%PROJECT_ROOT%frontend"
echo [2/2] 前端生产构建...
if not exist node_modules call npm install
call npm run build
if errorlevel 1 goto :failed
popd

echo.
echo [成功] 后端测试和前端构建均通过。
pause
exit /b 0

:failed
echo.
echo [失败] 请查看上方红色错误信息。
popd
pause
exit /b 1
