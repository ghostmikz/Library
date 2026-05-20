@echo off
cd /d "%~dp0"

echo Checking Java version...
javac -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: javac not found. Install JDK 17 or later and add it to PATH.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

set LIBS=lib\*
set SRC=lib-server\src
set OUT=lib-server\out

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling server...
dir /s /b "%SRC%\*.java" > "%TEMP%\lib_server_src.txt"
javac --release 17 -cp "%LIBS%" -d "%OUT%" @"%TEMP%\lib_server_src.txt"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Compilation failed. See errors above.
    echo Make sure you have JDK 17 or later installed.
    pause
    exit /b 1
)

echo Starting Library Server on port 9091...
java -cp "%OUT%;%LIBS%" server.LibraryServer
pause
