@echo off
REM Build script for HL7 Test Bench (Windows)
REM Requires JDK 17 or higher

setlocal enabledelayedexpansion

echo ============================================
echo  HL7 Test Bench Build Script
echo ============================================
echo.

REM Check for Java compiler
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: javac not found. Please install JDK 17 or higher.
    echo Download from: https://adoptium.net/temurin/releases/
    exit /b 1
)

REM Try to find jar.exe
set JAR_CMD=

REM Method 1: Check if jar is directly available
where jar >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    for /f "delims=" %%i in ('where jar') do (
        set JAR_CMD=%%i
        goto :found_jar
    )
)

REM Method 2: Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set JAR_CMD=%JAVA_HOME%\bin\jar.exe
        goto :found_jar
    )
)

REM Method 3: Search common JDK locations
for %%D in (
    "C:\Program Files\Eclipse Adoptium"
    "C:\Program Files\Java"
    "C:\Program Files\Microsoft"
    "C:\Program Files\Zulu"
    "C:\Program Files\Amazon Corretto"
) do (
    if exist %%D (
        for /f "delims=" %%J in ('dir /b /s %%D\jar.exe 2^>nul') do (
            set JAR_CMD=%%J
            goto :found_jar
        )
    )
)

REM Method 4: Search Program Files for any JDK
for /f "delims=" %%J in ('dir /b /s "C:\Program Files\*jdk*\bin\jar.exe" 2^>nul') do (
    set JAR_CMD=%%J
    goto :found_jar
)

echo ERROR: jar.exe not found.
echo.
echo Please do one of the following:
echo   1. Set JAVA_HOME environment variable to your JDK installation
echo   2. Add your JDK's bin directory to the PATH
echo.
echo Example: set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot
echo.
exit /b 1

:found_jar
echo Found jar at: %JAR_CMD%
echo.

REM Show Java version
echo Using Java:
javac -version
echo.

REM Create output directories
echo Creating build directories...
if not exist out\classes mkdir out\classes
if not exist out\jar mkdir out\jar

REM Clean previous build
del /q out\classes\*.* 2>nul
for /d %%p in (out\classes\*) do rmdir /s /q "%%p" 2>nul

REM Find all Java source files
echo Compiling source files...
dir /s /b src\main\java\*.java > sources.txt

REM Compile
javac -d out\classes -sourcepath src\main\java @sources.txt 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    del sources.txt
    exit /b 1
)
del sources.txt

echo Compilation successful!
echo.

REM Create manifest
echo Creating JAR manifest...
echo Main-Class: com.hl7testbench.HL7TestBench> out\MANIFEST.MF
echo.>> out\MANIFEST.MF

REM Create JAR
echo Building JAR file...
cd out\classes
"%JAR_CMD%" cfm ..\jar\HL7TestBench.jar ..\MANIFEST.MF .
cd ..\..

if exist out\jar\HL7TestBench.jar (
    echo.
    echo ============================================
    echo  Build successful!
    echo ============================================
    echo.
    echo JAR file created: out\jar\HL7TestBench.jar
    echo.
    echo To run the application:
    echo   java -jar out\jar\HL7TestBench.jar
    echo.
) else (
    echo ERROR: Failed to create JAR file
    exit /b 1
)

endlocal
