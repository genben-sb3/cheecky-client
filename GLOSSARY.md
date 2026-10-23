# Cheecky Client

Clientseitige Minecraft-Mod mit Utility- und Cheat-Funktionen für Singleplayer und Server, die das erlauben.

## Language

**Modul**:
Eine einzelne Funktion, die man an- und abschalten kann, z. B. KillAura oder X-Ray.
_Avoid_: Hack, Cheat, Feature (für einzelne Module)

**Feature**:
Alles am Client, was kein Modul ist, z. B. ClickGUI, Navigator oder Chat-Befehle.
_Avoid_: Modul (für diese Dinge)

**Chat-Befehl**:
Eingabe im Chat, die mit `.` beginnt, nur lokal ausgeführt und nie an den Server geschickt wird.
_Avoid_: Command, Kommando

**Anarchy-Server**:
Server, dessen Regeln Hacks ausdrücklich erlauben. Zusammen mit dem Singleplayer der vorgesehene Einsatzort.

**Geladene Chunks**:
Der Teil der Welt, den der Client gerade kennt (Sichtweite). Module, die etwas finden oder anzeigen, sehen nur diesen Teil.

**Freund**:
Spieler, den Kampf-Module nie angreifen, z. B. Mitglieder des eigenen Clans.
_Avoid_: Teammitglied, Verbündeter

**Kategorie**:
Gruppe von Modulen, die im ClickGUI ein gemeinsames Fenster hat, z. B. Combat oder Movement.
