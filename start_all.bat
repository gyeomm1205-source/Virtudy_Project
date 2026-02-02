@echo off
echo [Start] Virtudy Integrated Development Environment

:: 1. Start AI Server (Background)
echo [1/3] Starting AI Server...
start "Virtudy AI Server" cmd /k "cd /d .\AI\concentration_monitor && ..\..\.venv\Scripts\python.exe server.py"

:: 2. Start Backend (Background)
echo [2/3] Starting Backend Server...
start "Virtudy Backend" /min cmd /k "cd /d .\BE && gradlew bootRun --args='--spring.profiles.active=local'"

:: 3. Start Frontend
echo [3/3] Starting Frontend...
cd /d .\FE\virtudy-frontend
echo Ready to start Frontend. Waiting for Backend/AI to warm up...
timeout /t 5
pnpm run dev
