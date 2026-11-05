@echo off
setlocal EnableExtensions
title V1Mod - JAR Build
cd /d "%~dp0"
echo ============================================
echo   V1Mod 2.0 - Minecraft 1.21.1 / NeoForge
echo ============================================
echo.

if not exist build.gradle (
  echo FEHLER: build.gradle fehlt. Bitte im v1mod-Projektordner ausfuehren.
  pause
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo FEHLER: Java wurde nicht gefunden.
  echo Bitte Java JDK 21 installieren: https://adoptium.net/temurin/releases/?version=21
  pause
  exit /b 1
)

REM Gradle Wrapper ist im Projekt nicht enthalten. Gradle 8.10.2 wird automatisch heruntergeladen.
set "GRADLE_HOME=%USERPROFILE%\.v1mod-tools\gradle-8.10.2"
set "GRADLE_ZIP=%USERPROFILE%\.v1mod-tools\gradle-8.10.2-bin.zip"
set "GRADLE_EXE=%GRADLE_HOME%\bin\gradle.bat"

if not exist "%GRADLE_EXE%" (
  echo Gradle 8.10.2 wird heruntergeladen. Das kann kurz dauern...
  if not exist "%USERPROFILE%\.v1mod-tools" mkdir "%USERPROFILE%\.v1mod-tools"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 (
    echo FEHLER: Gradle Download fehlgeschlagen. Bitte Internetverbindung pruefen.
    pause
    exit /b 1
  )
  echo Gradle wird entpackt...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%GRADLE_ZIP%' -DestinationPath '%USERPROFILE%\.v1mod-tools' -Force"
  if errorlevel 1 (
    echo FEHLER beim Entpacken von Gradle.
    pause
    exit /b 1
  )
)

echo.
echo Mod wird gebaut. Beim ersten Start werden NeoForge-Dateien heruntergeladen.
call "%GRADLE_EXE%" build
if errorlevel 1 (
  echo.
  echo BUILD FEHLGESCHLAGEN. Bitte sende mir einen Screenshot dieses Fensters.
  pause
  exit /b 1
)

echo.
echo BUILD ERFOLGREICH!
echo Die JAR-Datei befindet sich hier:
echo %~dp0build\libs
explorer "%~dp0build\libs"
pause
