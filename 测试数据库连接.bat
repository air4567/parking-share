@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   测试数据库连接
echo ========================================
echo.

echo 正在测试 MySQL 连接...
echo 配置信息：
echo - 地址: localhost:3306
echo - 数据库: parking_share
echo - 用户名: root
echo - 密码: root
echo.

mysql -u root -proot -e "USE parking_share; SHOW TABLES;" 2>nul
if %errorlevel% == 0 (
    echo.
    echo [成功] 数据库连接正常！
    echo.
    echo 正在显示数据库中的表...
    mysql -u root -proot -e "USE parking_share; SHOW TABLES;"
) else (
    echo.
    echo [失败] 数据库连接失败
    echo.
    echo 可能的原因：
    echo 1. MySQL 服务未启动
    echo 2. 数据库 parking_share 不存在
    echo 3. 用户名或密码错误
    echo 4. MySQL 未添加到 PATH 环境变量
    echo.
    echo 请检查：
    echo - MySQL 服务是否运行
    echo - 是否已创建数据库：CREATE DATABASE parking_share;
    echo - 是否已执行 schema.sql 初始化表结构
)

echo.
echo ========================================
pause
