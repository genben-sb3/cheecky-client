# Schnell reisen auf 6b6t (MC 26.2) – Recherche

Stand: 2026-10-23. Ziel: herausfinden, wie man auf 6b6t legitim große Strecken (Tausende bis Hunderttausende Blöcke) schnell zurücklegt, und was der Cheecky Client daran automatisieren kann. „Legitim“ heißt hier: Server-Befehle und Vanilla-Bewegung, keine Anticheat-Umgehung, keine Exploits, keine Dupes.
**Nicht im Umfang:** Anticheat-Bypässe, Geschwindigkeiten über Vanilla oder über veröffentlichten Limits, Exploits (auch Teleport-Exploits) und Dupes. Fundstellen zu solchen Themen (z. B. Wiki-Seiten über Teleport-Exploits oder Bypass-Add-ons) wurden bewusst nicht ausgewertet.
Was schon in [6b6t-regeln-limits.md](6b6t-regeln-limits.md) und [perlenkanone.md](perlenkanone.md) steht (Elytra-Vertikallimit 3,0 Blöcke/Tick, Versionen, Perlen-Bug unter Folia), wird hier nur verlinkt.

Alle Quellen wurden am **2026-10-23** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Spiel-Jar 26.2 (`minecraft-merged-201bed67e3-26.2.jar`), Mojang-Namen, gelesen mit `javap -c -p -constants` | Spielcode | Primärquelle |
| [6b6t.org/en/commands](https://6b6t.org/en/commands), [/en/shop](https://6b6t.org/en/shop), [/en/shop/legend](https://6b6t.org/en/shop/legend), [/en/vote](https://6b6t.org/en/vote), [/en/history](https://6b6t.org/en/history), [/en/rules](https://6b6t.org/en/rules), [6b6t.org](https://6b6t.org/) | Betreiber | offiziell |
| [blog.6b6t.org](https://blog.6b6t.org/): [Spawn verlassen](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/) (15.09.2024, geändert 14.10.2026), [Vote-Belohnungen](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/) (15.11.2024, geändert 18.10.2026), [Ultra-Ränge](https://blog.6b6t.org/why-you-should-buy-ultra-ranks/) (28.12.2024, Update 14.10.2026), [The Highway Men](https://blog.6b6t.org/the-group-behind-the-6b6t-highways-the-highway-men/) (14.12.2024), [Seed](https://blog.6b6t.org/what-is-the-seed-of-6b6t/) (03.09.2024), [Kits](https://blog.6b6t.org/where-to-find-kits/) | Betreiber (Autor „qbasty“) | offiziell |
| [github.com/6b6t/feedback](https://github.com/6b6t/feedback) Issues #1, #2, #4, #15, #17, #28 | Betreiber | offiziell, aber interne Arbeitsnotizen |
| [6b.wiki](https://6b.wiki/) (Seiten Spawn, Spawn Escaping, The Highway Men, 2026 Nether Roof Event, Commands) | Community-Wiki, von der offiziellen History-Seite verlinkt | **nicht offiziell**, nur als Ergänzung |
| [cabaletta/baritone](https://github.com/cabaletta/baritone), Branch `26.2`: `Settings.java`, `ElytraCommand.java`, `ElytraProcess.java`; [Releases](https://github.com/cabaletta/baritone/releases) | Quellcode | Primärquelle |
| [minecraft.wiki: Boat](https://minecraft.wiki/w/Boat), [Happy Ghast](https://minecraft.wiki/w/Happy_Ghast) | Wiki | zum Abgleich mit dem Code |

Nicht gelesen: der 6b6t-Discord (nur per Login erreichbar). Der Quellcode der Teleport-Plugins (`6b6t/6b6t-plugins`) ist privat (GitHub-API: 404).

---

## 1. Server-Befehle auf 6b6t

### 1.1 Was es gibt (offizielle Befehlsliste)

| Befehl | Rang | Zweck | Quelle |
|---|---|---|---|
| `/tpa <spieler>` | Free | Teleport-Anfrage an einen Spieler | [Commands](https://6b6t.org/en/commands) |
| `/tpy <spieler>`, `/tpn <spieler>` | Free | Anfrage annehmen / ablehnen | [Commands](https://6b6t.org/en/commands) |
| `/toggletpa` (`/tptoggle`, `/tpt`) | Free | Anfragen von allen ausblenden | [Commands](https://6b6t.org/en/commands) |
| `/sethome <name>`, `/home <name>`, `/homes`, `/delhome`, `/homerename`, `/homecancel` | Free | Homes setzen, nutzen, verwalten, laufenden Home-Teleport abbrechen | [Commands](https://6b6t.org/en/commands) |
| `/hotspot` | Free | Teleport zum gerade aktiven Hotspot | [Commands](https://6b6t.org/en/commands) |
| `/hotspot create` | Elite, Elite Ultra, Apex, Legend | Hotspot am eigenen Standort anlegen | [Commands](https://6b6t.org/en/commands) |
| `/kill` (`/suicide`) | Free | Sich selbst töten | [Commands](https://6b6t.org/en/commands) |
| `/freecam` | Free | Vanilla-Freecam zum Auskundschaften | [Commands](https://6b6t.org/en/commands) |

**Nicht** in der offiziellen Liste: `/tpahere`, `/back`, `/spawn`, `/warp`, `/rtp` (Volltextsuche im HTML von [/en/commands](https://6b6t.org/en/commands): 0 Treffer für „tpahere“, „/back“, „tpcancel“). Das Community-Wiki führt dieselbe Liste ohne diese Befehle ([6b.wiki: Commands](https://6b.wiki/Wiki/Commands)). `/tpcancel` existiert im Plugin, ist laut Betreiber-Issue aber für normale Spieler durch die Befehls-Whitelist gesperrt ([Issue #15](https://github.com/6b6t/feedback/issues/15), Abschnitt „Prerequisite“).

### 1.2 Cooldowns und Ausführungszeit (`/tpa`, `/home`)

Laut Shop gilt der `/tpa`-Cooldown als „Time you must wait between sending /tpa requests“ und der `/home`-Cooldown als „Time you must wait between /home & /sethome uses“ ([Shop](https://6b6t.org/en/shop)). „Execution Time“ ist die Wartezeit, bis der Teleport ausgeführt wird ([Legend](https://6b6t.org/en/shop/legend)).

| Stufe | Homes | `/tpa`-Cooldown | `/home`-Cooldown | Ausführungszeit | Quelle |
|---|---|---|---|---|---|
| ohne Rang, ohne Votes | 2 („first home when joining and second when you put on netherite armor“) | **nicht veröffentlicht** | **nicht veröffentlicht** | nicht veröffentlicht | [Vote-Blog](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/) |
| + AnarchyMod | +2 Homes, stapeln mit Votes und Rängen | – | – | – | [Vote](https://6b6t.org/en/vote) |
| 7 Votes (Tag 1) | 3 | – | – | – | [Vote-Blog](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/) |
| 21 Votes (Tag 3) **= Prime** | 3 (Prime: 12) | **420 s** | 300 s | nicht veröffentlicht | Vote-Blog, [Shop](https://6b6t.org/en/shop) |
| Prime Ultra (Abo) | 18 | 300 s | 180 s | nicht veröffentlicht | [Ultra-Blog](https://blog.6b6t.org/why-you-should-buy-ultra-ranks/) („still match the shop“, Update 14.10.2026) |
| 49 Votes (1 Woche) **= Elite** | 4 (Elite: 30) | **120 s** | 90 s | nicht veröffentlicht | Vote-Blog, Shop |
| Elite Ultra (Abo) | 50 | 70 s | 40 s | nicht veröffentlicht | Ultra-Blog |
| 210 Votes (1 Monat) **= Apex** | 5 (Apex: 100) | **30 s** | 15 s | Apex: **15 s** | Vote-Blog, Shop, [Legend](https://6b6t.org/en/shop/legend) |
| Legend | 500 | **10 s** | 10 s | **3 s** | [Legend](https://6b6t.org/en/shop/legend) („The /home and /tpa cooldowns are 10 seconds, with a 3-second teleport execution time.“) |

- Votes: 7 Seiten, je 1 Vote pro 24 h, also maximal 7 pro Tag. „The voting rewards on 6b6t are permanent.“ ([Vote-Blog](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/), [Vote](https://6b6t.org/en/vote)). Wer einen Monat lang täglich alle 7 Seiten votet, hat also kostenlos die Apex-Cooldowns (30 s / 15 s), aber nur 5 Homes (+2 mit AnarchyMod).
- Content-Creator-Ränge (YouTuber, TikTok) haben die Elite-Werte ([Vote-Blog](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/)).
- **Pro Ziel oder global?** Nicht ausdrücklich veröffentlicht. Die Shop-Formulierung „between sending /tpa requests“ spricht für **einen** Cooldown pro Absender, egal an wen ([Shop](https://6b6t.org/en/shop)). *Ableitung aus dem Wortlaut, nicht bestätigt.* Ob der Cooldown beim Senden oder erst nach einem erfolgreichen Teleport startet, steht nirgends.
- Der Hinweis des Nutzers „/tpa hat einen langen Cooldown“ passt zu den Werten ohne Votes/Rang. Die genaue Zahl für diese Stufe ist in keiner offiziellen Quelle zu finden (Website, Blog, Shop, Issues durchsucht).

### 1.3 Bedingungen für `/tpa` und `/home`

| Bedingung | Inhalt | Quelle |
|---|---|---|
| Spawn-Sperre für `/tpa` | „/tpa is turned off at spawn … It works again once your X or Z coordinate is above 5,000 or below -5,000. In the Nether, it is above 2,000 or below -2,000.“ | [Blog: Spawn verlassen](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/) |
| `/home` und `/hotspot` am Spawn | „/sethome, /home and /hotspot all work at spawn.“ | ebd. |
| PvP-Modus | „/home and /tpa do not work in PvP mode. It starts when you fight another player and ends after 60 seconds with no damage taken or dealt.“ | ebd. |
| Annahmefrist | „They have 60 seconds to accept it.“ | ebd. |
| Abbruch durch Schaden | Wird einer der beiden Spieler von einem Entity (Mob oder Spieler) verletzt oder schlägt selbst zu, werden **alle** seine laufenden Anfragen abgebrochen („Teleport failed!“). Umweltschaden (Fall, Feuer, Lava) bricht nicht ab. | [Issue #15](https://github.com/6b6t/feedback/issues/15), Tabelle Pfad 1 und „Other problems found“ |
| Fahrzeug | Einsteigen in ein Fahrzeug bricht ab. Sitzt der Anfragende beim Ausführen in einem Fahrzeug, schlägt der Teleport fehl. | [Issue #15](https://github.com/6b6t/feedback/issues/15), Pfade 2 und 6 |
| Laufender `/home` | Läuft gerade ein `/home`-Teleport, schlägt `/tpa` fehl. | [Issue #15](https://github.com/6b6t/feedback/issues/15), Pfad 7 |
| Ausloggen | Ist ein Spieler länger als 10 s offline, wird die Anfrage abgebrochen. | [Issue #15](https://github.com/6b6t/feedback/issues/15), Pfad 5 |
| Ankunft in ladendem Gebiet | Nach `/home` oder Teleport wird man manchmal (ca. 1 von 10 Versuchen in selten besuchten Gebieten) für ~5 s auf den Backup-Server geworfen. Nichts geht verloren. Offen. | [Issue #28](https://github.com/6b6t/feedback/issues/28) |

### 1.4 Respawn, Betten, `/kill`, Hotspots

- Wer ohne Bett stirbt, startet am Spawn: „If you die without a bed, you start at spawn again, but /home works there.“ ([Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/)). `/kill` „does the same as jumping: you die and respawn somewhere new“ ([Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/)).
- **Spawn-Radius:** Das offizielle Blog nennt die Spawn-Zone „5000 by 5000 blocks“ in den FAQ und die `/tpa`-Grenze bei |X| oder |Z| > 5.000 (Nether 2.000) ([Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/)). Das Community-Wiki beschreibt dieselbe Zone als (−5000, −5000) bis (5000, 5000) ([6b.wiki: Spawn](https://6b.wiki/Wiki/Spawn)). Die beiden Angaben „5000 by 5000“ und „±5000“ widersprechen sich in der Fläche; maßgeblich für `/tpa` ist die Koordinatenregel.
- **Bett als Teleport:** Mit gesetztem Bett bringt `/kill` einen zum Bett. Vanilla-Tod heißt aber: Inventar fällt am Todesort (ob 6b6t `keepInventory` nutzt, steht in keiner offiziellen Quelle). Bekannter Fehler: Bett-Spawnpunkte wurden zufällig gelöscht; laut Betreiber im Fox-Commit `6014011` behoben, aktiv nach dem nächsten Neustart ([Issue #2](https://github.com/6b6t/feedback/issues/2), Kommentar).
- **Hotspot:** Ein Hotspot ist ein temporärer Treffpunkt. „When one is active, /hotspot takes you there, even from spawn.“ ([Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/)). Dauer und Cooldown beim Anlegen: Elite 30 min / 30 min, Apex bis 90 min / 60 min, Legend bis 120 min / 80 min ([Shop](https://6b6t.org/en/shop)). Es gibt jeweils nur einen globalen Hotspot ([6b.wiki: Commands](https://6b.wiki/Wiki/Commands): „Teleport to the global hotspot“).

---

## 2. Vanilla-Geschwindigkeiten aus dem Spielcode (26.2)

Alle Werte aus dem Bytecode der 26.2-Jar. 1 Tick = 1/20 s. Endgeschwindigkeiten sind aus den Konstanten hergeleitet; wo ich simuliert habe, steht das dabei.

### 2.1 Boot auf Eis (`AbstractBoat`)

- Tick-Reihenfolge in `AbstractBoat.tick()`: `floatBoat()` (Reibung) → `controlBoat()` (Antrieb, nur beim steuernden Client) → `move()`.
- `floatBoat()` an Land: `deltaMovement.x/z *= landFriction`, `deltaRotation *= landFriction`. `landFriction` kommt aus `getGroundFriction()`: Mittelwert von `Block.getFriction()` aller Blöcke mit Kollisionsform direkt unter der Bootsbox (Luftlücken zählen nicht mit, Seerosen werden übersprungen).
- Im Wasser: Faktor `0.9f`, in der Luft ebenfalls `0.9f`.
- `controlBoat()`: vorwärts `+0.04f`, rückwärts `−0.005f`, nur lenken `+0.005f`. Links/rechts ändert `deltaRotation` um 1°/Tick.
- Reibungswerte aus `Blocks`: `ICE`, `PACKED_ICE`, `FROSTED_ICE` = **0.98f**, `BLUE_ICE` = **0.989f**, `SLIME_BLOCK` = 0.8f, Standard 0.6f.
- Endgeschwindigkeit `v = 0.04 / (1 − f)` Blöcke/Tick:

| Untergrund | f | Blöcke/Tick | **Blöcke/s** |
|---|---|---|---|
| Wasser | 0,9 | 0,40 | **8,0** |
| Eis / Packeis | 0,98 | 2,00 | **40,0** |
| Blaueis | 0,989 | 3,64 | **72,7** |

Stimmt mit dem Wiki überein: Wasser ≈ 8,0, Eis ≈ 40, Blaueis ≈ 72,72 Blöcke/s ([Wiki: Boat](https://minecraft.wiki/w/Boat)).

*Folge für Autopilot (hergeleitet):* Auch `deltaRotation` wird nur mit `f` gedämpft. Auf Blaueis läuft eine Drehung also mit 0,989 pro Tick nach. Ein Lenk-Regler muss gegensteuern, sonst übersteuert er.

### 2.2 Elytra mit Feuerwerksraketen

- **Gleiten** (`LivingEntity.updateFallFlyingMovement`): Auftrieb abhängig von `cos²(Pitch)`, Luftwiderstand `(0.99, 0.98, 0.99)`. Der Code entspricht der bekannten Formel, die Konstanten (`0.75`, `−0.1`, `0.04`, `3.2`, `0.1`) sind im Bytecode.
- **Raketen-Boost** (`FireworkRocketEntity.tick`, solange `attachedToEntity.isFallFlying()`): pro Tick `v += look·0.1 + (look·1.5 − v)·0.5`.
- **Brenndauer:** `lifetime = 10·(1 + flightDuration) + rand(6) + rand(7)` Ticks. Flugdauer 1: 20–31 Ticks (Ø 25,5), Flugdauer 3: 40–51 (Ø 45,5) (Konstruktor `FireworkRocketEntity`).
- **Haltbarkeit:** Elytra `durability(432)` (`Items.ELYTRA`). `LivingEntity.updateFallFlying` zieht 1 Punkt pro 20 Ticks Flug ab, also **432 s ≈ 7,2 min Flugzeit** pro Elytra ohne Verzauberung.
- **Simuliert** (eigenes Skript mit genau diesen Formeln, Schwerkraft 0,08):

| Fall | horizontal | vertikal |
|---|---|---|
| Dauerboost, Pitch 0° | **33,5 Blöcke/s** | −0,6 Blöcke/s |
| Dauerboost, Pitch −10° (leicht steigend) | 32,4 Blöcke/s | +6,2 Blöcke/s |
| Gleiten ohne Rakete, Pitch 0° | 30,2 Blöcke/s | −3,0 Blöcke/s (Gleitzahl ≈ 10:1) |
| Gleiten, Pitch +30° (sinkend) | 50,0 Blöcke/s | −6,8 Blöcke/s |

- Raketenverbrauch bei Dauerboost: 10.000 Blöcke ≈ 299 s ≈ **235 Raketen (Flugdauer 1)** oder **≈ 130 (Flugdauer 3)**; Elytra-Verschleiß ≈ 299 Punkte, also 69 % einer Elytra. Eine Elytra reicht für ≈ **14.400 Blöcke** im Overworld. *Hergeleitet.*
- Vertikal liegen alle Vanilla-Werte weit unter dem 6b6t-Limit von 3,0 Blöcken/Tick (60 Blöcke/s, siehe [6b6t-regeln-limits.md](6b6t-regeln-limits.md)). Laut Betreiber-Test löst sogar „steep dive to terminal speed, then a hard pull-up“ mit Vanilla-Client keinen Setback aus ([Issue #4](https://github.com/6b6t/feedback/issues/4)).

### 2.3 Nether-Maßstab (`DimensionType`)

- `DimensionTypes` setzt für `NETHER` `coordinateScale = 8.0`, Overworld und End 1.0. `DimensionType.getTeleportationScale(a, b)` = `a.coordinateScale / b.coordinateScale` (Bytecode).
- Nether-Höhe 256, logische Höhe 128 (Bytecode `DimensionTypes`). Das Bedrock-Dach liegt bei y = 127.
- **Jede Nether-Geschwindigkeit zählt im Overworld achtfach.**

### 2.4 Pferd (`AbstractHorse`)

- `generateSpeed`: `(0.45 + 0.3·r + 0.3·r + 0.3·r) · 0.25` → Attribut 0,1125 bis **0,3375**, Mittel 0,225.
- Am Boden: Beschleunigung = Attribut (`getFrictionInfluencedSpeed`: `speed · 0.21600002 / f³`, bei Standardblock f = 0,6 gleich 1), Dämpfung `0.6 · 0.91`, Eingabe `0.98` (`INPUT_FRICTION`). Endgeschwindigkeit = `speed · 0.98 / (1 − 0.546) · 20`:
  - bestes Pferd (0,3375): **14,6 Blöcke/s**
  - Durchschnittspferd (0,225): **9,7 Blöcke/s**
- Eis bringt Pferden fast nichts: auf Blaueis ergibt dieselbe Formel ≈ +1 % (hergeleitet).

### 2.5 Happy Ghast (gibt es in 26.2)

- Klasse `net.minecraft.world.entity.animal.happyghast.HappyGhast`. `FLYING_SPEED = 0.05`. Geritten: Eingabevektor × `3.9 · 0.05`, `travelFlying` mit Faktor `0.05 · 5/3`, Luftwiderstand `0.91` (`LivingEntity.travelFlying`).
- Endgeschwindigkeit ≈ 0,177 Blöcke/Tick = **3,5 Blöcke/s** (hergeleitet). Wiki: „about 3.6 m/s“, „unaffected by Speed“ ([Wiki: Happy Ghast](https://minecraft.wiki/w/Happy_Ghast)).
- Langsamer als Gehen. Für Langstrecken unbrauchbar, nur zum Bauen und für Mitfahrer.

### 2.6 Windladung (`WindCharge`)

- Explosionsstärke `1.2f`, Rückstoß-Multiplikator `1.22f` (`WindCharge`). Nach der Explosionsformel aus [perlenkanone.md](perlenkanone.md), Abschnitt 1.1, gibt das höchstens ≈ 1,2 Blöcke/Tick Stoß bei 0 Abstand.
- Nur ein Starthelfer (z. B. für den Elytra-Start), kein Langstreckenmittel.

### 2.7 Dreizack mit Riptide (`TridentItem`)

- Stärke aus `riptide.json`: `1.5 + 0.75·(Stufe−1)` → Riptide III = **3,0 Blöcke/Tick** Stoß in Blickrichtung. Mindestens 10 Ticks Aufladen, nur bei `isInWaterOrRain()`.
- Simuliert (Luft: horizontal × 0,91, vertikal (v − 0,08) × 0,98): Riptide III fliegt ≈ 29 Blöcke weit pro Wurf bei ≈ 26 Ticks Flugzeit, also ≈ **20–22 Blöcke/s** im Dauerbetrieb. Jeder Wurf kostet 1 Haltbarkeit.
- Nur bei Regen oder im Wasser. Unzuverlässig.

### 2.8 Zu Fuß

Gehen 0,1 · 0,98 / 0,454 · 20 = **4,3 Blöcke/s**, Sprinten (× 1,3) **5,6 Blöcke/s** (gleiche Formel wie 2.4, Spieler-Grundtempo 0,1).

### 2.9 Übersicht: Zeit für 10.000 und 100.000 Overworld-Blöcke

| Methode | Blöcke/s (Overworld) | 10.000 Blöcke | 100.000 Blöcke |
|---|---|---|---|
| Happy Ghast | 3,5 | 47 min | 7,8 h |
| Gehen / Sprinten | 4,3 / 5,6 | 39 / 30 min | 6,4 / 4,9 h |
| Boot auf Wasser | 8,0 | 21 min | 3,5 h |
| Pferd (Ø / bestes) | 9,7 / 14,6 | 17 / 11 min | 2,9 h / 1,9 h |
| Riptide III (Regen) | ≈ 21 | ≈ 8 min | ≈ 1,3 h |
| Elytra gleiten / mit Raketen | 30 / 33,5 | 5,5 / 5,0 min | 55 / 50 min |
| Boot auf Eis | 40 | 4,2 min | 42 min |
| Boot auf Blaueis | 72,7 | 2,3 min | 23 min |
| **Nether:** Sprinten × 8 | 45 | 3,7 min | 37 min |
| **Nether:** Pferd (bestes) × 8 | 117 | 1,4 min | 14 min |
| **Nether:** Elytra + Raketen × 8 | **268** | **37 s** | **6,2 min** |
| **Nether:** Boot auf Blaueis × 8 | 582 | 17 s | 2,9 min |
| `/tpa` oder `/home` | unabhängig von der Strecke | Cooldown + 3–15 s | gleich |

Nether-Elytra pro 100.000 Overworld-Blöcke: 12.500 Nether-Blöcke, ≈ 373 s Flug, ≈ 290 Raketen (Flugdauer 1) und 373 Haltbarkeitspunkte, also knapp eine Elytra. *Hergeleitet.*

---

## 3. 6b6t-Limits und Infrastruktur für diese Methoden

| Thema | Stand | Quelle |
|---|---|---|
| Elytra vertikal | ≤ 3,0 Blöcke/Tick (interne Notiz, kann sich ändern) | [Issue #4](https://github.com/6b6t/feedback/issues/4), siehe [6b6t-regeln-limits.md](6b6t-regeln-limits.md) |
| Elytra horizontal | kein Zahlenwert veröffentlicht. „The generic horizontal check still applies to elytra movement.“ | [Issue #4](https://github.com/6b6t/feedback/issues/4) |
| Raketen | kein Limit veröffentlicht | [6b6t-regeln-limits.md](6b6t-regeln-limits.md), Abschnitt 5 |
| Boot / Eis | **kein Limit veröffentlicht**. Rules, ToS, Shop, Blog und Issue-Tracker nennen Boote und Eis nicht (Suche nach „boat“, „ice“ im Tracker ohne Treffer) | [Rules](https://6b6t.org/en/rules), [Issues](https://github.com/6b6t/feedback/issues) |
| **Nether-Dach** | **gesperrt.** „The nether roof is patched on 6b6t, so you cannot travel on top of it.“ Ausnahme: das Community-Event vom 24.–28.04.2026 („Players … traveled far out from spawn utilizing the Nether Roof“) | [Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/), [History](https://6b6t.org/en/history) |
| **Nether-Highways** | gibt es, von Spielern gebaut: „Highways are wide, obsidian roads. They lead for millions of blocks. You use them by walking or with an elytra.“ Sie werden regelmäßig von Withern und Spielern beschädigt. Gepflegt von der Gruppe „The Highway Men“ (THM) | [Blog: THM](https://blog.6b6t.org/the-group-behind-the-6b6t-highways-the-highway-men/), [History](https://6b6t.org/en/history) |
| Highway-Netz (Community-Angabe) | Laut Community-Wiki: alle Achsen-Highways bis zur Overworld-Weltgrenze gepflastert (02.03.2026), letzter Diagonal-Tunnel bis 3,75 Mio. Nether-Blöcke gegraben (08.05.2026). Gängige Breite „5 Blocks Wide“ | [6b.wiki: The Highway Men](https://6b.wiki/Wiki/The_Highway_Men) (nicht offiziell) |
| Eis-Highways | **keine Quelle gefunden**, weder offiziell noch im Community-Wiki (Wiki-Suche „ice highway“: kein Treffer) | – |
| Nether-Spawn | Laut Community-Wiki 2.000 × 2.000 bis zum Grund ausgehöhlt, „flyable“ | [6b.wiki: Spawn](https://6b.wiki/Wiki/Spawn) (nicht offiziell) |
| Neue Chunks | Fehlerbericht: Chunk-Generierung stoppte nach ≈ 1.000 Blöcken Elytra-Flug im Nether (geschlossen). Kick bei Ankunft in ladendem Gebiet (offen) | [Issue #1](https://github.com/6b6t/feedback/issues/1), [Issue #28](https://github.com/6b6t/feedback/issues/28) |
| Elytra-Setbacks | Offen: Setbacks beim Abheben und beim Brustplatten-Tausch, auch ohne Fly-Mod | [Issue #4](https://github.com/6b6t/feedback/issues/4) |
| Seed | „6b6t's seed is -4172144997902289642“ (gleiche Zahl wie der alte 2b2t-Seed) | [Blog: Seed](https://blog.6b6t.org/what-is-the-seed-of-6b6t/) |

**Folge:** Ein Eis-Boot im Nether wäre mit 582 Blöcken/s (Overworld-Äquivalent) das schnellste Vanilla-Mittel. Es ist auf 6b6t aber nur nutzbar, wo jemand eine Eisbahn gebaut hat, und dafür gibt es keinen Beleg. Selbst bauen ist teuer: 1 Blaueis = 9 Packeis = 81 Eis (Vanilla-Rezepte), also 81.000 Eis pro 1.000 Blöcke einspurige Bahn. *Hergeleitet.* Praktisch bleibt die **Elytra auf den Nether-Highways** das Standardmittel. Das Dach ist zu.

---

## 4. Was der Client legitim automatisieren kann

Grundsatz wie in [perlenkanone.md](perlenkanone.md): Der Client schickt nur Eingaben, die ein Spieler auch selbst machen könnte, und bleibt bei Vanilla-Physik. Er schickt keine erfundenen Positionen und keine Zusatzgeschwindigkeit.

### 4.1 Autopilot mit fester Richtung

- Yaw auf den Zielkurs setzen (`atan2` zur Zielkoordinate), Vorwärts und Sprint halten. Das sind dieselben Eingaben wie von Hand.
- Mit Elytra: Pitch regeln (z. B. 0° bis −10° bei Boost), Rakete zünden, wenn die Geschwindigkeit unter einen Wert fällt. Das macht `ElytraFly` im Projekt schon (AutoFirework, Mindesttempo, Elytra wechseln). Vertikale Geschwindigkeit hart auf ≤ 2,5 Blöcke/Tick begrenzen (Puffer zum 3,0-Limit, [6b6t-regeln-limits.md](6b6t-regeln-limits.md)).
- Sicherheitsstopps: niedrige Elytra-Haltbarkeit, wenige Raketen, Hindernis voraus (Raycast entlang der Flugbahn), Spieler in Reichweite (`EntityRadar`), Hunger.
- Wichtig für `/tpa`-Ketten: Schaden durch Mobs bricht alle laufenden Anfragen ab ([Issue #15](https://github.com/6b6t/feedback/issues/15)). Der Autopilot sollte vor einem `/tpa` anhalten und nicht in Kämpfe geraten.

### 4.2 Baritone-Elytra

Gelesen im Baritone-Quellcode, Branch `26.2`:

- **Version:** Baritone v1.19.0 ist „For Minecraft 26.2“, v1.20.0 „For Minecraft 26.3“ ([Releases](https://github.com/cabaletta/baritone/releases)). Cheecky spricht Baritone schon per Reflection an (`BaritoneBridge`, bisher `mine` und `gotoNear`).
- **Befehl:** `#elytra` fliegt zum aktuellen Ziel („The elytra command tells baritone to, in the nether, automatically fly to the current goal“). Ohne Ziel kommt „No goal has been set“ (`ElytraCommand.java`). Laut Hinweistext ist das Feature „experimental … intended for long distance travel in the Nether but will also work in the Overworld, using fireworks for vanilla boost. It will not work with any other mods ("hacks") for non-vanilla boost.“
- **Voraussetzungen** (`ElytraCommand`, `Settings`):
  - Native Bibliothek „nether-pathfinder“: nur 64-Bit x86 oder ARM, Windows/Linux/Mac (`#elytra supported` prüft das).
  - Elytra an, Raketen im Inventar. `elytraFireworkSpeed` = 1,2 Blöcke/Tick: unter diesem Tempo zündet Baritone eine Rakete. Baritone fliegt also im Mittel etwas langsamer als Dauerboost (2.2).
  - `elytraTermsAccepted` muss bestätigt werden.
  - **Seed:** „Baritone Elytra for use in the Nether wants to know the seed of the world you are in. If it doesn't have the correct seed, it will frequently backtrack.“ Standard ist der 2b2t-Seed `146008555100680`, und `elytraPredictTerrain` ist `false` („If false all chunks that haven't been loaded are assumed to be air“).
- **Für 6b6t:** `#set elytraNetherSeed -4172144997902289642` ([Blog: Seed](https://blog.6b6t.org/what-is-the-seed-of-6b6t/)) und `#set elytraPredictTerrain true`. Einschränkung laut Blog: Neues Land entsteht mit der Server-Version, „The closer your single player version is to the server's, the closer the match“. Der Server startete 2022 auf 1.12.2 ([History](https://6b6t.org/en/history)), alte Nether-Chunks sehen also anders aus als der Seed in 26.2 vorhersagt. *Hergeleitet: Die Vorhersage stimmt nur für Chunks, die mit neuerem Nether-Generator erzeugt wurden. Bereits geladene Chunks liest Baritone ohnehin direkt.*
- `elytraAllowAboveRoof` auf `false` lassen (Standard): Das Dach ist gesperrt (Abschnitt 3).
- Weitere nützliche Einstellungen: `elytraAutoSwap` (Standard an, ab Haltbarkeit 5), `elytraMinFireworksBeforeLanding` = 5, `elytraAllowEmergencyLand`, `elytraConserveFireworks`, `elytraAutoJump` („not advisable on laggy servers!“).
- Anbindung im Client: Ziel als `GoalXZ` setzen (wie `gotoNear` mit `ICustomGoalProcess`) und dann `#elytra` über den Befehls-Manager auslösen. Die genaue API-Methode habe ich nicht geprüft (nur `ElytraCommand` und `Settings` gelesen).

### 4.3 Eis-Boot steuern

- Der Client steuert das Boot selbst (`isLocalInstanceAuthoritative`, `controlBoat()` läuft nur dort) über `setInput(left, right, up, down)`. Ein Regler kann den Kurs halten, indem er links/rechts tippt.
- Dämpfung von `deltaRotation` auf Blaueis nur 0,989 pro Tick (2.1): kurze Impulse und Gegensteuern, sonst pendelt das Boot.
- Vor jedem `/tpa` oder `/home` aussteigen: Im Fahrzeug schlägt `/tpa` fehl ([Issue #15](https://github.com/6b6t/feedback/issues/15)).
- Nutzen auf 6b6t gering, weil keine Eisbahnen belegt sind (Abschnitt 3).

### 4.4 Highway folgen

- Highways laufen entlang der Achsen und Diagonalen (THM: „cardinal highways“, Diagonalen; [6b.wiki](https://6b.wiki/Wiki/The_Highway_Men), nicht offiziell). Ein Modul kann den Yaw auf 0°/90°/180°/270° oder 45°-Schritte einrasten und die Querabweichung zur Highway-Mitte (x = 0, z = 0 oder |x| = |z|) ausregeln.
- Hindernisse (Wither-Löcher, Blockaden) per Raycast nach vorn erkennen und anhalten oder an Baritone übergeben (`#goto` zu Fuß, `#elytra` im Flug).
- Laut Betreiber-Blog sind Highways regelmäßig beschädigt ([Blog: THM](https://blog.6b6t.org/the-group-behind-the-6b6t-highways-the-highway-men/)), laut Community-Wiki gibt es auch Gruppen, die sie absichtlich blockieren ([6b.wiki](https://6b.wiki/Wiki/The_Highway_Men)). Ein Autopilot braucht also einen sicheren Stopp.

---

## 5. `/tpa`-Ketten mit Clan-Mitgliedern

Ein `/tpa` überbrückt jede Entfernung. Kosten pro Sprung = max(Cooldown, Ausführungszeit + Annahmezeit). Ein Sprung ist nur möglich, wenn der Anfragende außerhalb der Spawn-Zone steht (|X| oder |Z| > 5.000, im Nether > 2.000), nicht im PvP-Modus ist, nicht im Fahrzeug sitzt und keinen Mob-/Spielerschaden bekommt (1.3).

| Stufe | Zeit pro Sprung (geschätzt) | Sprünge pro 10 min | Quelle der Eingangswerte |
|---|---|---|---|
| Legend | ≈ 10 s (Cooldown 10 s ≥ 3 s Ausführung + schnelle Annahme) | ≈ 60 | [Legend](https://6b6t.org/en/shop/legend) |
| Apex oder 210 Votes | ≈ 30 s (Cooldown 30 s; Ausführung 15 s passt hinein) | ≈ 20 | [Shop](https://6b6t.org/en/shop), [Vote-Blog](https://blog.6b6t.org/what-do-you-get-for-voting-on-minecraft-anarchy-6b6t/), [Legend](https://6b6t.org/en/shop/legend) |
| Elite Ultra | ≈ 70 s | ≈ 8 | [Ultra-Blog](https://blog.6b6t.org/why-you-should-buy-ultra-ranks/) |
| Elite oder 49 Votes | ≈ 120 s | 5 | Shop, Vote-Blog |
| Prime Ultra | ≈ 300 s | 2 | Ultra-Blog |
| Prime oder 21 Votes | ≈ 420 s | 1–2 | Shop, Vote-Blog |
| ohne Rang/Votes | unbekannt (laut Nutzer lang) | – | nicht veröffentlicht |

*Die Zeiten pro Sprung sind hergeleitet.* Annahmen: Der Cooldown gilt global pro Absender (1.2) und startet beim Senden, und die Ausführungszeiten unter Apex sind nicht bekannt.

**Folgerung:** Ketten lohnen sich fast nie. Ein einziger Sprung zum Mitglied, das dem Ziel am nächsten steht, ersetzt jede Kette, weil die Entfernung keine Rolle spielt. Ketten braucht man nur, wenn das Ziel-Mitglied erst durch Zwischen-Sprünge erreichbar ist (z. B. bei Annahme nur von Bekannten). Dann zählt nur die Anzahl der Sprünge, nicht die Strecke.

### Rangliste für einen Clan, der über die Karte verteilt ist

1. **Ein `/tpa` zum nächstgelegenen Mitglied, Rest mit Nether-Elytra.** 1 Sprung (Apex/210 Votes: ≈ 15–45 s inkl. Annahme), danach z. B. 20.000 Overworld-Blöcke Restweg = 2.500 Nether-Blöcke ≈ 75 s Flug. Zuverlässig, solange ein Mitglied online ist und annimmt.
2. **Home-Netz.** Homes an Highway-Kreuzungen, Portalen und Basen setzen. `/home` funktioniert auch **am Spawn**, `/tpa` nicht (1.3). Braucht niemanden online. Grenze ist die Zahl der Homes: ohne Rang 2 (+2 AnarchyMod, +3 durch 210 Votes = 7), Prime 12, Elite 30, Apex 100, Legend 500 ([Shop](https://6b6t.org/en/shop), [Legend](https://6b6t.org/en/shop/legend), [Vote](https://6b6t.org/en/vote)).
3. **„Relais“-Mitglieder an festen Punkten.** Ein Mitglied (oder Bot-Account) parkt an einem Highway-Ende und nimmt Anfragen an. Laut Community-Wiki macht THM genau das mit „KitBot1“: „teleport their members to the end of the nether highways on demand“ ([6b.wiki](https://6b.wiki/Wiki/The_Highway_Men), nicht offiziell). Offiziell ist nur belegt, dass es Bots gibt und man ihre Teleport-Anfragen ausblenden kann (`/bots teleports`, [Commands](https://6b6t.org/en/commands)).
4. **Echte Kette (mehrere `/tpa` hintereinander).** Nur mit kurzem Cooldown sinnvoll (Legend 10 s, Apex/210 Votes 30 s). Ab Elite 120 s ist jede zusätzliche Etappe langsamer als 4 km Nether-Flug (120 s × 33,5 Blöcke/s × 8 ≈ 32.000 Overworld-Blöcke). *Hergeleitet.*
5. **Hotspot.** Nur wenn gerade einer am passenden Ort aktiv ist. Funktioniert auch vom Spawn aus.
6. **Bett + `/kill`.** Bringt einen zum Bett, kostet aber das Inventar (Vanilla-Tod). Bett-Fehler ([Issue #2](https://github.com/6b6t/feedback/issues/2)) laut Betreiber behoben, aber nach dem Neustart noch nicht bestätigt. Nur mit leerem Inventar sinnvoll.

---

## 6. Was nicht gefunden wurde

- **Kein veröffentlichter `/tpa`- oder `/home`-Cooldown für Spieler ohne Rang und ohne Votes.** Auch keine Ausführungszeit für Prime, Elite und Ultra-Ränge.
- Nicht bestätigt, ob der `/tpa`-Cooldown pro Ziel oder global gilt und wann er startet (Plugin-Code privat).
- Kein offizielles Limit für Boote, Eis, Pferde oder horizontale Elytra-Geschwindigkeit.
- Keine Eis-Highways in offiziellen Quellen oder im Community-Wiki.
- Keine offizielle Highway-Karte. Die Angaben zum Netz (Weltgrenze, Diagonalen) stammen nur aus dem Community-Wiki.
- Ob 6b6t `keepInventory` nutzt, steht nirgends.
- Discord-Ankündigungen nicht gelesen (Login nötig). Die Änderungsseite [/en/updates](https://6b6t.org/en/updates) zeigt nur die letzten Einträge und erwähnt `/tpa`, Cooldowns, Boote und das Nether-Dach nicht.
- Keine Messung im Spiel. Elytra- und Riptide-Werte sind mit den Code-Formeln simuliert, alles andere aus Code-Konstanten hergeleitet.

---

## Rangliste für 6b6t (praktische Blöcke/s und Zuverlässigkeit)

„Praktisch“ = Overworld-Blöcke pro Sekunde über eine lange Strecke, mit Vorbereitung und Pausen grob eingerechnet. *Bewertung hergeleitet aus den Abschnitten 1–5.*

| Rang | Methode | Praktisch (Overworld-Blöcke/s) | Zuverlässigkeit | Warum |
|---|---|---|---|---|
| 1 | **`/tpa` oder `/home` + Nether-Elytra für den Rest** | Sprung: Strecke egal (10–45 s mit Apex/Votes); Rest ≈ 200–268 | hoch, wenn Home oder Mitglied vorhanden | Kombiniert den Sprung (Entfernung egal) mit dem schnellsten belegten Verkehrsweg. `/home` geht auch am Spawn. |
| 2 | **Nether-Highway + Elytra mit Raketen** (von Hand, Autopilot oder Baritone) | ≈ 200–268 (Vanilla-Maximum 268) | mittel–hoch | Highways sind offiziell belegt, Dach gesperrt. Kostet ≈ 290 Raketen und knapp eine Elytra pro 100.000 Blöcke. Risiken: Schäden am Highway, Setbacks beim Start ([#4](https://github.com/6b6t/feedback/issues/4)), Chunk-Laden ([#1](https://github.com/6b6t/feedback/issues/1), [#28](https://github.com/6b6t/feedback/issues/28)). |
| 3 | **Nether-Highway zu Fuß (Sprint) oder mit Pferd** | 45 (Sprint) bis 117 (bestes Pferd) | hoch | Keine Verbrauchsgüter (Pferd: muss erst durchs Portal und gefunden werden). Gut für einen Autopiloten mit Highway-Verfolgung. |
| 4 | `/tpa`-Kette über mehrere Mitglieder | je Sprung 10 s (Legend) bis 420 s (Prime/21 Votes) | mittel | Hängt an Online-Mitgliedern, Schaden bricht ab, Spawn-Sperre. Mit 30 s Cooldown gut, ab 120 s langsamer als Fliegen. |
| 5 | Overworld-Elytra mit Raketen | 30–33,5 | mittel | Kein Highway nötig, aber 8× langsamer als im Nether. |
| 6 | Boot auf Blaueis (Nether ×8 = 582) | theoretisch 72,7 / 582 | niedrig | Keine Eisbahnen auf 6b6t belegt, Bau sehr teuer. |
| 7 | Pferd, Boot auf Wasser, Riptide, Happy Ghast (Overworld) | 3,5–15 (Riptide ≈ 21 bei Regen) | gering bis mittel | Langsam oder wetterabhängig. |
