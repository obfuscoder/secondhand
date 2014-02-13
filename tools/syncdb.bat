@echo off
:loop
tools\java\bin\java.exe -jar bin\secondhand-filesync-1.1.0.jar
timeout /t 1
goto loop
