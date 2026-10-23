# Cheecky Client

Fabric-Client-Mod für Minecraft **26.3** und **26.2** mit Wurst-artigem ClickGUI. Für jede Version gibt es eine eigene Datei: `cheecky-mc26.3-….jar` bzw. `cheecky-mc26.2-….jar`. Nur für Singleplayer und Server, die so etwas erlauben.

## Module

⚠️ = fällt auf Servern mit Anti-Cheat sehr wahrscheinlich auf.

| Kategorie | Modul | Taste | Was es macht |
|---|---|---|---|
| Combat | KillAura | R | Greift das nächste Ziel in Reichweite an |
| Combat | TriggerBot | – | Schlägt zu, wenn das Fadenkreuz auf einem Ziel liegt |
| Combat | Criticals | – | KillAura/TriggerBot springen und treffen im Fallen kritisch |
| Combat | AutoTotem | – | Totem automatisch in die Zweithand |
| Combat | AutoArmor | – | Zieht die beste Rüstung an (Elytra bleibt) |
| Combat | AutoAnchor | – | Respawn-Anker neben Gegner setzen, laden, zünden (Selbstschutz, ≥ 100 ms) |
| Combat | AutoCrystal | – | End-Kristalle neben Gegner setzen und zünden, rechnet Rüstung und Eigenschaden mit (≥ 100 ms) |
| Combat | AntiKnockback | – | Kein Rückstoß durch Treffer und Explosionen |
| Movement | Flight ⚠️ | G | Fliegen wie im Kreativmodus, mit Anti-Kick gegen den Vanilla-Kick |
| Movement | ElytraFly | – | Einfacher Start, AutoFirework, Stopp im Wasser, Ersatz-Elytra |
| Movement | Speed ⚠️ | – | Schneller laufen (Modi Strafe/Vanilla) |
| Movement | BunnyHop | B | Springt automatisch beim Laufen |
| Movement | Step ⚠️ | – | Blöcke hochlaufen wie Stufen |
| Movement | Spider ⚠️ | – | Wände hochklettern |
| Movement | Jesus ⚠️ | – | Über Wasser und Lava laufen |
| Movement | NoSlow | – | Kein Abbremsen beim Essen, in Spinnweben, auf Seelensand |
| Movement | NoFall | – | Kein Fallschaden |
| Movement | Autopilot | – | Hält auf dem Highway die Richtung (zu Fuß oder Elytra), stoppt bei Withern, Hindernissen, Hunger, kaputter Elytra |
| Movement | AutoSprint | – | Sprintet automatisch |
| Movement | Freecam | V | Kamera vom Körper lösen, Körper bleibt stehen |
| Render | X-Ray | X | Nur Erze und Spawner sichtbar |
| Render | Fullbright | – | Alles taghell |
| Render | Koordinaten | – | Unten rechts: Position, umgerechnete Nether-/Oberwelt-Koordinate, Blickrichtung (standardmäßig an) |
| Render | EntityRadar | – | Markiert Endermen, Wither und optional Spieler durch Wände, meldet neue im Chat |
| Render | BlockFinder | – | Markiert Holz, Erze oder eigene Blöcke mit Box und Linie, `.finder goto` lässt Baritone hinlaufen |
| Player | PearlAim | – | Zielt die Enderperle automatisch auf eine mit `.pearl` festgelegte Stelle, zeigt die Flugbahn |
| World | AutoTool | – | Bestes Werkzeug, Behutsamkeit für Glas/Eis usw., Warnung wenn nichts droppt |
| World | FastBreak | – | Keine Pause zwischen Blöcken, optional schnellerer Abbau |
| World | AutoMine | – | Lässt Baritone Holz oder Erze abbauen (Baritone nötig, siehe unten) |

Jedes Modul hat im ClickGUI eine Beschreibung (Maus drüberhalten) und eigene Einstellungen.

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
| `.baritone` (kurz `.b`) | Baritone-Menü öffnen |
| `.friend add Name` / `.friend remove Name` / `.friend list` (kurz `.f`) | Freunde: KillAura, TriggerBot, AutoAnchor und AutoCrystal greifen sie nie an |
| `.finder` | die 5 nächsten Funde des BlockFinders mit Koordinaten |
| `.finder add ancient_debris` / `.finder remove …` / `.finder list` | eigene Blockliste für BlockFinder und AutoMine (Ziel „Eigene Liste“) |
| `.finder goto` | Baritone läuft zum nächsten Fund |
| `.home add Base` / `.home add Stash 120000 -5000` / `.home list` / `.home remove Base` | Merkt sich, wo deine /home-Punkte liegen |
| `.home nearest 250000 0` | Welcher Home liegt am nächsten an diesem Ziel (Oberwelt-Koordinaten)? |
| `.pearl 100 64 -200` / `.pearl here` / `.pearl clear` (kurz `.p`) | Ziel für PearlAim: Koordinaten, Block im Fadenkreuz (bis 256 Blöcke) oder löschen |

Modulnamen sind egal bei Groß-/Kleinschreibung und Bindestrichen: `xray` findet `X-Ray`.

Alles wird in `config/cheecky.json` gespeichert.

## Bauen

Braucht Java 25 (hier installiert unter `~/.local/share/jdks/jdk-25.0.5+8`):

```fish
set -x JAVA_HOME ~/.local/share/jdks/jdk-25.0.5+8
./build-all.sh         # baut für 26.3 und 26.2 nach build/libs/
./gradlew runClient    # Testclient starten
```

## In CurseForge benutzen

1. **Create Custom Profile** → Minecraft **26.3** (oder **26.2**, z. B. für 6b6t), Loader **Fabric**.
2. Im Profil **Fabric API** hinzufügen.
3. **Open Folder** → die passende `cheecky-mc<minecraft-version>-<version>.jar` aus den [Releases](https://github.com/genben-sb3/cheecky-client/releases) in den Ordner `mods/` kopieren.

## Baritone (Menü und AutoMine)

**Baritone-Menü:** Knopf **„Baritone“** im Pausenmenü (neben „Cheecky“), Chat-Befehl `.b` oder eine eigene Taste (Optionen → Steuerung → Cheecky, standardmäßig frei). Darin:

- **Gehe zu:** X, Y, Z eintragen (Y leer lassen = Baritone sucht die Höhe selbst), „Hier“ trägt deine Position ein, „Los“ läuft hin.
- **Folgen:** Spielername eintragen, Baritone läuft hinterher.
- **Block:** z. B. `diamond_ore` oder `oak_log`, dann „Hinlaufen“ oder „Abbauen“.
- **Erkunden**, **Farmen**, **Stopp**.
- Schalter für Baritone-Einstellungen: Abbauen, Platzieren, Parkour, Sprinten, Inventar, Freier Blick. Baritone speichert sie selbst.
- **Elytra-Flug zu X/Z:** Baritone fliegt mit Elytra und Raketen zum Ziel, gedacht für den Nether. Auf 6b6t trägt Cheecky den offiziellen Seed und die Gelände-Vorhersage selbst ein. Beim ersten Mal verlangt Baritone im Chat, dass du `#set elytraTermsAccepted true` eingibst.
- **Koords: Oberwelt/Nether:** in welchen Koordinaten du X/Y/Z einträgst. Cheecky rechnet für die Dimension, in der du bist, selbst um (÷ bzw. × 8).
- Oben steht, was Baritone gerade tut und wie lange es noch ungefähr dauert.

Das Modul **AutoMine** (World) steuert **Baritone**: Ziel wählen (Holz, Diamant, Eisen, Gold, Antiker Schrott …), einschalten, und Baritone läuft selbst los und baut ab. Ausschalten stoppt Baritone. Baritone ist ein eigener Mod und muss zusätzlich in den `mods/`-Ordner:

| Minecraft | Datei | Download |
|---|---|---|
| 26.3 | `baritone-api-fabric-1.20.0.jar` | https://github.com/cabaletta/baritone/releases/tag/v1.20.0 |
| 26.2 | `baritone-26.2-20260813.152229-1.jar` (Meteor-Build, läuft ohne Meteor) | https://maven.meteordev.org/snapshots/meteordevelopment/baritone/26.2-SNAPSHOT/baritone-26.2-20260813.152229-1.jar |

- Für 26.3 die **api-fabric**-Datei nehmen, **nicht** „standalone“: In der standalone-Datei ist die Schnittstelle verschleiert, die Cheecky braucht.
- Nur **eine** Baritone-Datei in `mods/` legen.
- Ohne Baritone ist AutoMine im ClickGUI grau und die Knöpfe im Baritone-Menü sind gesperrt.
- Alle normalen Baritone-Befehle gehen weiterhin im Chat mit `#`: `#mine diamond_ore`, `#tunnel`, `#goto x y z`, `#farm`, `#stop`.

## Bekannte Grenzen

- **6b6t** läuft auf 26.2 und verlangt zusätzlich **AnarchyMod**. Ob Cheecky und AnarchyMod zusammen laufen, ist noch nicht getestet.

- X-Ray funktioniert nicht zusammen mit **Sodium**, dafür bräuchte es eigene Mixins.
- Keine Anti-Cheat-Umgehung. Auf Servern mit Anti-Cheat fallen KillAura, Flight, NoFall usw. auf.
- Flight auf Servern nur mit `allow-flight=true`, sonst wirft der Server dich raus.
