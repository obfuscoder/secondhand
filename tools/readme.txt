Einrichtung:
============

Für Betrieb an einem einzigen System:
-------------------------------------
1. Prüfen, dass die Systemuhrzeit korrekt ist (möglichst Sekundengenau!)
2. flohmarkt.zip entpacken nach flohmarkt Ziel
3. Datenbank starten mit tools\xampp\xampp-control.exe -> Start-Button bei Mysql
4. Datenbank auf neuestem Stand bringen mit tools\heidisql\heideisql.exe -> CTRL+O -> Dump aus data\db\dump_neuestesdatum.sql wählen -> schauen dass Datenbank "floh" links ausgewählt ist -> Playbutton (oder F9) drücken

Zusätzlich für Synchronisierung mit weiteren Systemen und mit Backups auf USB-Stick:
------------------------------------------------------------------------------------
5. ALLE Dateien in data\sync löschen! Ansonsten werden die Dateien beim Starten von sync.bat gleich als verkauft markiert
6. USB-Stick einstecken (Verzeichnis "data\sync" darauf löschen, wenn vorhanden!),
7. In sync.bat den Pfad zum USB-Stick aktualisieren. Aktuell eingestellt ist "h:\data\sync". Es sollte das Verzeichnis <Laufwerksbuchstabe>:\data\sync benutzt werden.
8. In sync.bat Datei den Namen bzw. die IP-Adresse des Servers zum Synchronisieren der Dateien eintragen - aktuell steht da "socket://server:31337/data/sync". Es sollte auf zentrales System zeigen (für aktuellen Flohmarkt ist hier "schleppi" vorgesehen).

Applikationen im Betrieb:
=========================

Als erstes: Datenbank starten mit tools\xampp\xampp-control.exe -> Start-Button bei Mysql

- gui reicht für einfachen Betrieb
- im verteiltem system und aus sicherheit sollten die anderen programme aber auch gestartet werden

gui.bat
-------
Startet die Applikation für Abrechnungen, Stornos, Verkäufe, Testscans etc.

backup.bat
----------
Führt alle 5 Minuten ein Backup der wichtigsten Daten aus der Datenbank in das Verzeichnis data\sync mit Dateinamen "dump_<rechnername>_<datum>.sql.gz" durch

sync.bat
--------
Synchronisiert Datenbankeinträge der Verkäufe und Stornos mit dem Unterverzeichnis data\sync\refunded bzw data\sync\sold und synchronisiert wiederum diesen Ordner mit einem Ordner auf einen möglichen angeschlossenen USB-Stick. Außerdem wird eine Synchronisierung des data\sync-Ordners mit dem eingetragen Server versucht.

Für Server:
===========

Diese applikationen werden nur auf dem Server ausgeführt, der als Austauschknoten für die Verkaufsinformationen genutzt werden soll. Er wird in die sync.bat der anderen Rechner eingetragen.

syncserver.bat
--------------
Startet den synch-Server. Das Unterverzeichnis data\sync wird durch Zugriffe der Clients als Datenhalde benutzt

syncdb.bat
----------
Synchronisiert den data\sync-Folder mit der lokalen Datenbank.
