@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   测试 MySQL 密码
echo ========================================
echo.

echo 正在测试常见的 MySQL root 密码...
echo.

REM 测试密码 root
echo [测试 1] 测试密码: root
mysql -u root -proot -e "SELECT 1;" >nul 2>&1
if %errorlevel% == 0 (
    echo [成功] 密码是: root
    echo 请修改 application.yml 中的 password 为: root
    goto :end
)

REM 测试空密码
echo [测试 2] 测试空密码
mysql -u root -e "SELECT 1;" >nul 2>&1
if %errorlevel% == 0 (
    echo [成功] 密码为空（无密码）
    echo 请修改 application.yml 中的 password 为: （留空）
    goto :end
)

echo [失败] 无法自动检测密码
echo.
echo 请手动测试：
echo 1. 打开命令行
echo 2. 运行: mysql -u root -p
echo 3. 输入你的 MySQL root 密码
echo 4. 如果成功连接，说明密码正确
echo 5. 然后修改 application.yml 中的 password 为你的实际密码
echo.

:end
echo.
echo ========================================
pause
