@echo off
REM Library Server — compile and start
cd /d "%~dp0"

set LIBS=lib\*
set SRC=lib-server\src
set OUT=lib-server\out

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling server...
dir /s /b "%SRC%\*.java" > "%TEMP%\lib_server_sources.txt"
javac -cp "%LIBS%" -d "%OUT%" @"%TEMP%\lib_server_sources.txt"

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

echo Starting Library Server on port 9091...
java -cp "%OUT%;%LIBS%" server.LibraryServer
pause
