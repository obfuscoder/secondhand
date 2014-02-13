@echo off
:loop
tools\unison\unison.exe -socket 31337
timeout /t 5
goto loop
