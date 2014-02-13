@echo off
:loop
for /f "delims=" %%i in ('tools\unix\date +%%Y%%m%%d%%H%%M%%S') do set datestr=%%i
echo Creating database backup
tools\xampp\mysql\bin\mysqldump.exe -u root --single-transaction floh reserved_items customers purchases | tools\unix\gzip.exe > data\sync\dump_%COMPUTERNAME%_%datestr%.sql.gz
timeout /t 300
goto loop
