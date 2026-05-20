@echo off
setlocal enabledelayedexpansion
REM Library Client — compile and start
cd /d "%~dp0"

set LIBS=lib\*
set SRC=lib-client\src
set OUT=lib-client\out

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling client...
dir /s /b "%SRC%\*.java" > "%TEMP%\lib_client_sources.txt"
javac -cp "%LIBS%" -d "%OUT%" @"%TEMP%\lib_client_sources.txt"

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

REM Copy resource files to output
for /r "%SRC%" %%f in (*.properties *.png *.jpg *.gif) do (
    set "REL=%%f"
    set "REL=!REL:%SRC%\=!"
    set "DEST=%OUT%\!REL!"
    for %%d in ("!DEST!") do if not exist "%%~dpd" mkdir "%%~dpd"
    copy /y "%%f" "%OUT%\!REL!" >nul
)

echo Starting Library Client...
java ^
    -Dawt.useSystemAAFontSettings=lcd ^
    -Dswing.aatext=true ^
    -cp "%OUT%;%LIBS%" App
pause
