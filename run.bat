@echo off
REM ================================================
REM SISTEMA PDV OFFLINE - Script de Inicializacao
REM ================================================

REM Caminho do JDK 17 (ajuste se instalou em outro local)
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot

REM Caminho do Maven (ajuste se instalou em outro local)
set MAVEN_HOME=C:\Users\david\apache-maven-3.9.6

REM Adiciona ao PATH
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ================================================
echo   SISTEMA PDV OFFLINE
echo   Iniciando...
echo ================================================
echo.

REM Vai para o diretorio do projeto
cd /d "%~dp0"

REM Compila e executa o sistema
mvn spring-boot:run

echo.
echo Sistema encerrado.
pause
