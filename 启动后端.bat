@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   小区共享车位管理系统 - 后端服务启动
echo ========================================
echo.

cd /d %~dp0

REM 尝试查找 Java
set JAVA_CMD=java
where java >nul 2>&1
if %errorlevel% neq 0 (
    REM 尝试从常见位置查找 Java
    if exist "C:\Program Files\Java\jdk1.8.0_*\bin\java.exe" (
        for /d %%i in ("C:\Program Files\Java\jdk1.8.0_*") do set JAVA_CMD=%%i\bin\java.exe
    ) else if exist "C:\Program Files\Java\jdk-*\bin\java.exe" (
        for /d %%i in ("C:\Program Files\Java\jdk-*") do set JAVA_CMD=%%i\bin\java.exe
    ) else if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    ) else (
        echo [错误] 未找到 Java 命令
        echo.
        echo 请尝试以下方法：
        echo 1. 确保已安装 JDK 1.8 或更高版本
        echo 2. 将 Java 的 bin 目录添加到系统 PATH 环境变量
        echo 3. 或者使用 IDE（如 IntelliJ IDEA）直接运行 ParkingApplication.java
        echo.
        echo 如果已安装 Java，请手动设置 PATH：
        echo 右键"此电脑" -^> "属性" -^> "高级系统设置" -^> "环境变量"
        echo 在"系统变量"的 Path 中添加 Java 的 bin 目录路径
        echo.
        pause
        exit /b 1
    )
)

echo 正在检查 Java 环境...
"%JAVA_CMD%" -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Java 无法运行
    pause
    exit /b 1
)
"%JAVA_CMD%" -version

REM 尝试查找 Maven
set MVN_CMD=mvn
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    REM 尝试从常见位置查找 Maven
    if exist "C:\Program Files\Apache\maven\*\bin\mvn.cmd" (
        for /d %%i in ("C:\Program Files\Apache\maven\*") do set MVN_CMD=%%i\bin\mvn.cmd
    ) else if exist "C:\apache-maven-*\bin\mvn.cmd" (
        for /d %%i in ("C:\apache-maven-*") do set MVN_CMD=%%i\bin\mvn.cmd
    ) else if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd
    ) else (
        echo [错误] 未找到 Maven 命令
        echo.
        echo 请尝试以下方法：
        echo 1. 确保已安装 Maven 3.6 或更高版本
        echo 2. 将 Maven 的 bin 目录添加到系统 PATH 环境变量
        echo 3. 或者使用 IDE（如 IntelliJ IDEA）直接运行，IDE 会自动处理 Maven
        echo.
        echo 如果已安装 Maven，请手动设置 PATH：
        echo 右键"此电脑" -^> "属性" -^> "高级系统设置" -^> "环境变量"
        echo 在"系统变量"的 Path 中添加 Maven 的 bin 目录路径
        echo.
        pause
        exit /b 1
    )
)

echo.
echo 正在检查 Maven 环境...
"%MVN_CMD%" -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Maven 无法运行
    pause
    exit /b 1
)
"%MVN_CMD%" -version

echo.
echo ========================================
echo 正在启动后端服务...
echo 服务地址: http://localhost:8080/api
echo.
echo 提示: 按 Ctrl+C 可以停止服务
echo ========================================
echo.

"%MVN_CMD%" spring-boot:run

if %errorlevel% neq 0 (
    echo.
    echo [错误] 启动失败
    echo.
    echo 请检查：
    echo 1. MySQL 数据库是否已启动
    echo 2. application.yml 中的数据库配置是否正确
    echo 3. 端口 8080 是否被占用
    echo.
)

pause
