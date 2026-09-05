# Metis
Ein Todo-App Management Programm
<hr>

| Erstellt im Rahmen der Semesteraufgabe Informatik 2 (Sommersemester 2026)                                                                                                                                              |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Systembeschreibung:** A (Aufgabenverwaltung)                                                                                                                                                                         |
| **Github Repository:** https://github.com/ecasept/metis (möglicherweise noch nicht öffentlich)                                                                                                                         |

Die Dokumentation ist verfügbar als:
- **Markdown Dateien:** `README.md` und `BUILD.md`
- **Website:** https://semesteraufgabe.babo.zapto.org/about und https://semesteraufgabe.babo.zapto.org/build

## Ausführungshinweise
Das Programm ist in zwei Teile gegliedert, den Server und den Client.
Der Client ist das eigentliche Todo-Management Programm mit Benutzeroberfläche.
Der Server ist ein optionales Zusatzteil, das die Speicherung von Tasks und Synchronisation mit weiteren Geräten über ein Account-System erlaubt.

In der Abgabe sind zwei Ordner: `remote` und `local`.
Der `remote` Ordner enthält eine client `.jar` Datei die ausgefürt werden kann.
Sie enthält alle nötigen Dependencies.
Dieser Client ist mit unserem [remote server](https://semesteraufgabe.babo.zapto.org/about) konfiguriert den wir hosten.
Man braucht also keinen extra Server aufzusetzen.

Falls man dennoch einen lokalen Server testen will, kann man in den `local` ordner schauen.
Dort sind zwei `.jars` und einige Konfigurationsdateien vorhanden.
Die client `.jar` ist ein Client der für den lokalen server konfiguriert ist.
Die server jar ist der Server fürs lokale Hosten.
Jedoch braucht der Server etwas Konfiguration.
Diese sind in Form der `.env` Datei und der `keystore.jks` schon bereitgestellt.
Führe als ersten den Server in einen anderen Terminal aus.
Während der Server am Laufen ist, kann nun der Client gestartet und verwendet werden.

### Selbst kompilieren
Bitte schaue in die `BUILD.md` Datei, dort ist der gesamte Prozess beschrieben und erklärt (Wenn du kein Englisch kannst dann frag AI). Kurz gesagt:
- Server kann mit `./gradlew :server:run` kompiliert und ausgeführt werden.
- Client kann mit `./gradlew :client:run` kompiliert und ausgeführt werden.
- Der Client kann mithilfe der `gradle.properties` zwischen der lokalen und der remote version konfiguriert werden.

## Kurzer Überblick über die Architektur
- `shared.serialization` enthält den Serialisierer der für Kommunikation zwischen Client und Server verwendet wird.
- `shared.db.querybuilder` enthält den Query builder, der die Datenbankabfragen generiert.
- `shared.sync` enthält den Kern der Synchronisationslogik, zusammen mit den beiden `SyncService` Klassen
- `DataManager` koordiniert die meiste business logik
- `MainFrame` enthält den Großteil des UI codes
- `client.ui` Der Rest der UI
- `server.api` enthält die server routes
- `server.security` security-related code
- `shared.models` Datenstrukturen die überall im Code verwendet werden
- Die beiden `DatabaseRepository` übernehmen die Schnittstelle zur Datenbank.

Anmerkung zu den zusätzlichen Features:
- der query builder ist ein bisschen unnötig und nervig weil wir eigentlich ein orm machen wollten, aber compile-time code generation hat dann zeitlich doch nicht mehr so ganz hingehauen und wir wollten nicht nochmal reflection machen
- die sync logic hat noch einige bugs und edge cases und wahrscheinlich viele antipatterns von denen wir noch nix wissen, da es das erste mal war dass wir mit distributed systems gearbeitet habe, aber wir wollten ja primär was lernen




 ## Hinweise zur Benutzung
 ### Aufgabenübersicht
 Nach dem Start des Programms erscheint zunächst die Hauptseite des Programms. Auf dieser Seite werden alle Aufgaben angezeigt, die der Benutzer angelegt hat. Jede Zeile stellt eine Aufgabe dar.  
 Die Priorität einer Aufgabe wird durch ein farbiges Quadrat markiert. Ist das Quadrat rot ist die Priorität der Aufgabe hoch, ist es gelb ist die Priorität mittel und ist keine Quadrat sichtbar, so ist die Priorität der Aufgabe niedrig.

 ### Ansicht wechseln
 Über den Menu-Button `Ansicht` kann ausgewählt werden, ob nur ausstehende, nur erledigte oder alle Aufgaben angezeigt werden. Außerdem kann über die Suchleiste gezielt nach Aufgaben gesucht werden.

 ### Aufgaben anlegen
 Über den Button `Neue Aufgabe` kann eine neue Aufgabe angelegt werden.
 Hierbei **müssen** ein Titel, eine Priorität und eine Fälligkeitsdatum angegeben werden. Das Fälligkeitsdatum darf nicht in der Vergangenheit liegen.  
 **Optional** können eine Uhrzeit zur Präzisierung des Fälligkeitsdatums und eine Beschreibung angegeben werden.

### Aufgaben anzeigen, bearbeiten und löschen
Über das Icon mit dem Stift kann eine Aufgabe bearbeitet werden. Hierzu öffnet sich eine Übersicht über die Aufgabe in der Änderungen vorgenommen werden können.

Möchte man die Aufgabe hingegen nur vollständig angezeigt bekommen **ohne** sie zu ändern kann man in der Übersicht auf den Titel der Aufgabe klicken. Es öffnet sich dieselbe Ansicht wie bei der Bearbeitung einer Aufgabe, nur ohne die Möglichkeit Änderungen vorzunehmen.

Möchte man eine Aufgabe löschen klickt man in der Übersicht auf den "Papierkorb"-Button und bestätigt anschließend die Aktion.

### Status einer Aufgabe ändern
Um den Status ("erldigt" oder "ausstehend") einer Aufgabe zu ändern klickt man in der Übersicht auf die Checkbox am Beginn der Zeile der jeweiligen Aufgabe.  
Aufgaben bei denen die Checkbox nicht markiert ist haben den Status "ausstehend", Aufgaben mit markierter Checkbox den Status "erledigt".

### Accountverwaltung
Ist man angemeldet kann man sich über den Menu-Button (`Account`) entweder abmelden (`Abmelden`) oder den eigenen Account löschen (`Account löschen`).
Ist man nicht angemeldet findet keine Synchronisation der eigenen Aufgaben mit dem Server statt.  
Entsprechend funktioniert der Button `Sync` nur wenn man angemeldet ist. 

 ## Zeitliche Überschneidungen am Tag der Abnahme
Für uns ist die Abnahme am 23.07.2026 flexibel ab 12:00 Uhr möglich.  
Vor 12:00 Uhr ist für uns **keine Abnahme** möglich, da ein Teammitglied am Vormittag eine andere Prüfung hat.
