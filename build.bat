@echo off
echo ========================================
echo Spring Cloud Alibaba Demo - Build Script
echo ========================================
echo.

echo [1/3] Cleaning project...
call mvn clean

echo.
echo [2/3] Installing dependencies...
call mvn install -DskipTests

echo.
echo [3/3] Build complete!
echo.
echo ========================================
echo Next Steps:
echo 1. Start Nacos Server (see NACOS_GUIDE.md)
echo 2. Create configurations in Nacos
echo 3. Run service-provider: cd service-provider && mvn spring-boot:run
echo 4. Run service-consumer: cd service-consumer && mvn spring-boot:run
echo ========================================
echo.

pause

