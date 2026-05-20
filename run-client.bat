@echo off
setlocal enabledelayedexpansion
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
set SRC=lib-client\src
set OUT=lib-client\out

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling client...
dir /s /b "%SRC%\*.java" > "%TEMP%\lib_client_src.txt"
javac --release 17 -cp "%LIBS%" -d "%OUT%" @"%TEMP%\lib_client_src.txt"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Compilation failed. See errors above.
    echo Make sure you have JDK 17 or later installed.
    pause
    exit /b 1
)

REM Copy resource files
for /r "%SRC%" %%f in (*.properties *.png *.jpg *.gif) do (
    set "REL=%%f"
    set "REL=!REL:%SRC%\=!"
    set "DEST=%OUT%\!REL!"
    for %%d in ("!DEST!") do if not exist "%%~dpd" mkdir "%%~dpd"
    copy /y "%%f" "%OUT%\!REL!" >nul
)

echo Starting Library Client...
java -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -cp "%OUT%;%LIBS%" App
pause
