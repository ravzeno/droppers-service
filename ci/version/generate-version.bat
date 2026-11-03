@echo off
echo 🔍 Extracting version and app name from build.gradle.kts...

rem Get version and app name from build.gradle.kts
for /f "tokens=3 delims= " %%i in ('findstr /r "^version" build.gradle.kts') do set version=%%i
for /f "tokens=3 delims= " %%i in ('findstr /r "^group" build.gradle.kts') do set appname=%%i

rem Remove double quotes from version and appname
set version=%version:"=%
set appname=%appname:"=%

echo 🔍 Getting git commit hash...

rem Get git commit hash
for /f %%i in ('git rev-parse --short HEAD') do set commit=%%i

echo 🔍 Getting git branch...

rem Get git branch
for /f %%i in ('git rev-parse --abbrev-ref HEAD') do set branch=%%i

echo 🔍 Creating JSON...

rem Create JSON
set json={"version": "%version%", "name": "%appname%", "gitCommit": "%commit%", "gitBranch": "%branch%"}

echo 📝 Writing JSON to file in the main resources folder...

rem Write JSON to file in the main resources folder
echo %json% > src\main\resources\app-info.json

echo ✅ Done!
