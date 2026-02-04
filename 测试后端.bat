@echo off
chcp 65001 >nul
echo ========================================
echo   测试后端服务是否运行
echo ========================================
echo.

echo 正在测试后端服务...
echo 测试地址: http://localhost:8080/api/community/list
echo.

curl -s http://localhost:8080/api/community/list >nul 2>&1
if %errorlevel% == 0 (
    echo [成功] 后端服务运行正常！
    echo.
    echo 正在获取数据...
    curl http://localhost:8080/api/community/list
) else (
    echo [失败] 后端服务未运行或无法访问
    echo.
    echo 请检查：
    echo 1. 后端服务是否已启动（运行 启动后端.bat）
    echo 2. 端口 8080 是否被占用
    echo 3. 防火墙是否允许 8080 端口
)

echo.
echo ========================================
pause
