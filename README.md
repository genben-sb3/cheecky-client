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

**Rechte Shift-Taste** öffnet das ClickGUI (änderbar unter Optionen → Steuerung → Cheecky). Alternativ: Knopf **„Cheecky“** im Pausenmenü (Esc).

Im ClickGUI:
- **Linksklick** auf ein Modul schaltet es an oder aus (grün = an).
- **Pfeil rechts** oder **Rechtsklick** öffnet die Einstellungen.
- Bei „Taste“ klicken und dann eine Taste drücken. Entf, Backspace oder Esc entfernt die Taste.
- Fenster lassen sich an der Titelleiste verschieben und mit `-` minimieren.
- **Navigator:** Einfach lostippen, z. B. „kill“, dann zeigt das ClickGUI nur noch passende Module. Backspace löscht, Esc leert die Suche.

## Chat-Befehle

Beginnen mit `.` und gehen **nie** an den Server. Antworten erscheinen nur bei dir mit grünem `[Cheecky]`.

| Befehl | Wirkung |
|---|---|
| `.help` | alle Befehle |
| `.list` | alle Module, aktive in Grün |
| `.toggle xray` (kurz `.t xray`) | Modul an/aus |
| `.bind killaura r` / `.bind killaura none` | Taste belegen / entfernen |
| `.binds` | alle belegten Tasten |
| `.settings killaura` | Einstellungen anzeigen |
| `.set killaura reichweite 4.5` | Einstellung ändern (Schalter: `an`/`aus`) |

Modulnamen sind egal bei Groß-/Kleinschreibung und Bindestrichen: `xray` findet `X-Ray`.

Alles wird in `config/cheecky.json` gespeichert.

## Bauen

Braucht Java 25 (hier installiert unter `~/.local/share/jdks/jdk-25.0.5+8`):

```fish
set -x JAVA_HOME ~/.local/share/jdks/jdk-25.0.5+8
./gradlew build        # Ergebnis: build/libs/cheecky-<version>.jar
./gradlew runClient    # Testclient starten
```

## In CurseForge benutzen

1. **Create Custom Profile** → Minecraft **26.3**, Loader **Fabric**.
2. Im Profil **Fabric API** hinzufügen.
3. **Open Folder** → die `cheecky-<version>.jar` aus den [Releases](https://github.com/genben-sb3/cheecky-client/releases) in den Ordner `mods/` kopieren.

## Materialien sammeln mit Baritone

Für automatisches Laufen und Abbauen gibt es **Baritone** als eigenen Mod:

1. Von https://github.com/cabaletta/baritone/releases die Datei `baritone-standalone-fabric-1.20.0.jar` (für MC 26.3) laden.
2. In denselben `mods/`-Ordner legen wie Cheecky.
3. Im Spiel: `#mine diamond_ore`, `#tunnel`, `#goto x y z`, `#farm`, `#stop`.

## Bekannte Grenzen

- X-Ray funktioniert nicht zusammen mit **Sodium**, dafür bräuchte es eigene Mixins.
- Keine Anti-Cheat-Umgehung. Auf Servern mit Anti-Cheat fallen KillAura, Flight, NoFall usw. auf.
- Flight auf Servern nur mit `allow-flight=true`, sonst wirft der Server dich raus.
