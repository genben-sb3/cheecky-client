# Cheecky Client

Fabric-Client-Mod für Minecraft 26.3 mit Wurst-artigem ClickGUI. Nur für Singleplayer und Server, die so etwas erlauben.

## Module

| Kategorie | Modul | Standard-Taste | Einstellungen |
|---|---|---|---|
| Combat | KillAura | R | Reichweite, Spieler/Monster/Tiere |
| Combat | AntiKnockback | – | Horizontal %, Vertikal % |
| Movement | Flight | G | Tempo |
| Movement | BunnyHop | B | Nur beim Sprinten |
| Movement | NoFall | – | – |
| Movement | AutoSprint | – | – |
| Render | X-Ray | X | Flüssigkeiten |
| Render | Fullbright | – | – |

**Rechte Shift-Taste** öffnet das ClickGUI (änderbar unter Optionen → Steuerung → Cheecky).

Im ClickGUI:
- **Linksklick** auf ein Modul schaltet es an oder aus (grün = an).
- **Pfeil rechts** oder **Rechtsklick** öffnet die Einstellungen.
- Bei „Taste“ klicken und dann eine Taste drücken. Entf, Backspace oder Esc entfernt die Taste.
- Fenster lassen sich an der Titelleiste verschieben und mit `-` minimieren.

Alles wird in `config/cheecky.json` gespeichert.

## Bauen

Braucht Java 25 (hier installiert unter `~/.local/share/jdks/jdk-25.0.5+8`):

```fish
set -x JAVA_HOME ~/.local/share/jdks/jdk-25.0.5+8
./gradlew build        # Ergebnis: build/libs/cheecky-1.0.0.jar
./gradlew runClient    # Testclient starten
```

## In CurseForge benutzen

1. **Create Custom Profile** → Minecraft **26.3**, Loader **Fabric**.
2. Im Profil **Fabric API** hinzufügen.
3. **Open Folder** → `build/libs/cheecky-1.0.0.jar` in den Ordner `mods/` kopieren.

## Bekannte Grenzen

- X-Ray funktioniert nicht zusammen mit **Sodium**, dafür bräuchte es eigene Mixins.
- Keine Anti-Cheat-Umgehung. Auf Servern mit Anti-Cheat fallen KillAura, Flight, NoFall usw. auf.
- Flight auf Servern nur mit `allow-flight=true`, sonst wirft der Server dich raus.
