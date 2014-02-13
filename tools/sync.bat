@echo off
:loop
tools\java\bin\java.exe -jar bin\secondhand-filesync-1.1.0.jar
timeout /t 1
tools\unison\unison.exe data\sync socket://server:31337/data/sync -terse -logfile log\unison.log -batch -dontchmod -perms 0 -ignorearchives -backup "Name *" -backupdir data\backup
tools\unison\unison.exe data\sync h:\data\sync -terse -logfile log\unison.log -batch -dontchmod -perms 0 -ignorearchives -backup "Name *" -backupdir data\backup
timeout /t 1
goto loop
