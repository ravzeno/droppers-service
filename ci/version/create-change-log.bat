@echo off

rem Setup file path
for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set datetime=%%i
set release_date=%datetime:~0,4%-%datetime:~4,2%-%datetime:~6,2% %datetime:~8,2%:%datetime:~10,2%:%datetime:~12,2%

rem Prompt user to enter version number
set /p version="🚀 Enter the version number (e.g., 1.0.0): "

set change_log_base_dir=.\changelog
set change_log_file=%version%.md
set changelog_path=%change_log_base_dir%\%change_log_file%

rem Ensure the directory exists and is writable
if not exist "%change_log_base_dir%" mkdir "%change_log_base_dir%"

rem Check if file already exists
if exist "%changelog_path%" (
    rem File exists, prompt user for confirmation
    set /p overwrite_confirm="❓ Changelog file for version %version% already exists. Do you want to overwrite? (y/n): "
    if /i not "%overwrite_confirm%"=="y" (
        echo ℹ️ Operation cancelled. No changes made.
        exit /b 0
    )
)

rem Create or overwrite the release notes file
(
    echo # [%version%] - %release_date%
    echo.
    echo ## Added
    echo.
    echo -
    echo.
    echo ## Changed
    echo.
    echo -
    echo.
    echo ## Fixed
    echo.
    echo -
    echo.
    echo ## Deprecated
    echo.
    echo -
    echo.
    echo ## Removed
    echo -
) > "%changelog_path%"

echo 🚀 Changelog file created or updated: %changelog_path%
