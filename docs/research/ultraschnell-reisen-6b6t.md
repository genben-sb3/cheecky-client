# Ultraschnell reisen auf 6b6t (MC 26.2) – Recherche

Stand: 2026-10-23. Ziel: Gibt es auf 6b6t legitime Wege, die **deutlich schneller** sind als Nether-Elytra und `/tpa`/`/home`? Untersucht wurden End-Gateways, Portalnetze, experimentelle Loren, Riptide-Dreizack mit Elytra, Happy Ghast und Respawn-Tricks. „Legitim“ heißt: Vanilla-Mechanik und Server-Befehle, keine Anticheat-Umgehung, keine Exploits, keine Dupes.
**Nicht im Umfang:** Bypässe, Teleport-Exploits, Dupes. Wo der Anticheat-Code gelesen wurde, steht hier nur, **ob und wo** ein Vanilla-Verhalten an eine Grenze stößt, nicht wie man sie umgeht.

Nicht wiederholt wird, was schon hier steht:
- [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md): `/tpa`- und `/home`-Cooldowns, Spawn-Sperre, Nether-Maßstab ×8, Elytra mit Raketen (33,5 / 268 Blöcke/s), Pferd, Eis-Boot, Happy Ghast (3,5 Blöcke/s), Riptide **ohne** Elytra (≈ 21 Blöcke/s), Baritone-Elytra, Nether-Dach gesperrt, Bett + `/kill` (kostet Inventar).
- [6b6t-regeln-limits.md](6b6t-regeln-limits.md): Regeln, NCP-Fork, vertikales Elytra-Limit 3,0 Blöcke/Tick.
- [perlenkanone.md](perlenkanone.md): Perlen, Perlen-Bug unter Folia.

Alle Quellen wurden am **2026-10-23** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Spiel-Jar 26.2 (`minecraft-merged-201bed67e3-26.2.jar`), Mojang-Namen, gelesen mit `javap -c -p -constants` | Spielcode | Primärquelle |
| Daten im Jar: `data/minecraft/datapacks/minecart_improvements/pack.mcmeta`, `worldgen/configured_feature/end_gateway_return.json`, `worldgen/placed_feature/end_gateway_return.json`, `enchantment/riptide.json`, `enchantment/unbreaking.json` | Spieldaten | Primärquelle |
| [github.com/6b6t/feedback](https://github.com/6b6t/feedback), alle 34 Issues per `gh` gelesen und durchsucht; relevant: #1, #2, #7, #8, #12, #16, #28 | Betreiber | offiziell, interne Arbeitsnotizen |
| [github.com/6b6t/NoCheatPlus](https://github.com/6b6t/NoCheatPlus) (Branch `master`, letzter Push 18.10.2026): `MovingConfig.java`, `CreativeFly.java`, `MovingListener.java`, `DefaultConfig.java`, `ModelFlying.java` | Betreiber-Fork des Anticheats | Quellcode, aber **die Live-Konfiguration ist nicht veröffentlicht** |
| [blog.6b6t.org](https://blog.6b6t.org/): alle 50 Beiträge aus der [Sitemap](https://blog.6b6t.org/sitemap-posts.xml) nach „gateway“, „minecart“, „riptide“, „trident“, „respawn“, „end portal“, „stronghold“, „border“ durchsucht; relevant: [Spawn verlassen](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/), [The Highway Men](https://blog.6b6t.org/the-group-behind-the-6b6t-highways-the-highway-men/), [3. Geburtstag](https://blog.6b6t.org/a-special-day-for-6b6t-3rd-birthday/) | Betreiber | offiziell |
| [6b6t.org](https://6b6t.org/), [/en/rules](https://6b6t.org/en/rules), [/en/updates](https://6b6t.org/en/updates), [/en/history](https://6b6t.org/en/history), [/en/commands](https://6b6t.org/en/commands), [/en/shop](https://6b6t.org/en/shop) | Betreiber | offiziell; nach denselben Begriffen durchsucht: **keine Treffer** zu Gateways, Loren, Riptide, Happy Ghast, Respawn-Radius |
| [6b.wiki: Spawn](https://6b.wiki/Wiki/Spawn) (Stand 08.06.2026), [The Highway Men](https://6b.wiki/Wiki/The_Highway_Men) | Community-Wiki | **nicht offiziell** |
| [minecraft.wiki: End gateway](https://minecraft.wiki/w/End_gateway), [Riptide](https://minecraft.wiki/w/Riptide), [Weather](https://minecraft.wiki/w/Weather), [Stronghold](https://minecraft.wiki/w/Stronghold) | Wiki | zum Abgleich mit dem Code |

Nicht lesbar: Fox (der Folia-Fork des Servers, `6b6t/Fox`) und `6b6t/6b6t-plugins` sind privat (GitHub-API: 404). Discord nicht gelesen.

---

## 1. End-Gateways

### 1.1 Spielcode

| Punkt | Wert | Fundstelle |
|---|---|---|
| Gateways um die Hauptinsel | bis zu **20**, eins pro Drachentod, Radius **96** um (0, 0), y = **75**, Winkelschritt 0,157 rad (= 2π/40) | `EnderDragonFight.spawnNewGateway()`: `96.0`, `0.15707963267948966`, `bipush 75` |
| Ziel beim ersten Benutzen | Richtung (Gateway-X, 0, Gateway-Z) normiert, **× 1024**. Ist der Chunk dort nicht leer, wird in 16er-Schritten **zurück**gegangen (max. 16 ×), danach über leere Chunks **vorwärts** (max. 16 × 16) | `TheEndGatewayBlockEntity.findExitPortalXZPosTentative`: `1024.0`, `-16.0`, `16.0`, Zähler `bipush 16` |
| Landepunkt | höchster Block im Radius 16 (bzw. 5 um ein bestehendes Gateway). Findet sich nichts, wird eine End-Insel erzeugt (`EndFeatures.END_ISLAND`). Dort entsteht ein Rück-Gateway, das ans Ursprungs-Gateway gekoppelt ist (`EndGatewayConfiguration.knownExit`) | `findOrCreateValidTeleportPos`, `getPortalPosition` |
| Natürliche Rück-Gateways in den Außeninseln | Feature `end_gateway_return`, Seltenheit 1/700 Chunks, Ziel fest **(100, 50, 0)** (`"exact": true`) | `placed_feature/end_gateway_return.json`, `configured_feature/end_gateway_return.json` |
| Abklingzeit | 40 Ticks nach jeder Benutzung (`triggerCooldown`, `bipush 40`); Strahl alle 2400 Ticks | `TheEndGatewayBlockEntity` |
| Wartezeit beim Betreten | **0 Ticks** (keine eigene `getPortalTransitionTime`, Standard von `Portal` ist 0) | `Portal.getPortalTransitionTime` |
| Schwung | Spieler behalten Geschwindigkeit und Blickrichtung (`Relative.union(DELTA, ROTATION)`), Perlen nicht (`Set.of()`) | `EndGatewayBlock.getPortalDestination` |

Das Wiki bestätigt 20 Gateways, 96 Blöcke, y 75, die 1024-Linie und dass man mit Elytra oder Perle „straight into the End gateway block“ muss, weil Grundgestein den Zugang zu Fuß versperrt ([Wiki: End gateway](https://minecraft.wiki/w/End_gateway)).

**Bewertung (hergeleitet):** Ein Gateway-Sprung überbrückt ≈ 1.000–1.300 Blöcke **innerhalb des Ends**, sofort und mit Schwung. Das End hat Maßstab 1 zur Overworld (`DimensionTypes`, siehe [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) 2.3) und keine Kopplung an Overworld-Koordinaten. Gateways bringen also **keinen Overworld-Fortschritt**. Sie nützen nur, um vom Hauptinsel-Bereich schnell in die Außeninseln zu kommen.

### 1.2 Zurück in die Overworld – und wo man landet

`EndPortalBlock.getPortalDestination` (Bytecode):
- **Overworld → End:** Ziel ist immer `ServerLevel.END_SPAWN_POINT` = **(100, 50, 0)**, darunter wird die Obsidian-Plattform gebaut (`EndPlatformFeature.createEndPlatform`). Egal, **wo** das Overworld-Portal steht.
- **End → Overworld (für Spieler):** `ServerPlayer.findRespawnPositionAndUseSpawnBlock(false, DO_NOTHING)`, also **zum eigenen Bett bzw. Seelenanker**. Fehlt der, gilt `TeleportTransition.missingRespawnBlock`/`createDefault`, also der Weltspawn.
- Der Parameter `false` heißt: Ein Seelenanker verliert dabei **keine Ladung** (`findRespawnAndUseSpawnBlock`, die Ladung wird nur bei `true` gesenkt).
- Wartezeit 0 Ticks (wie 1.1).

**Folge – der „End-Bett-Sprung“ (hergeleitet):** Irgendein End-Portal in der Overworld → (100, 50, 0) im End → ≈ 100 Blöcke zum Ausgangsportal bei (0, 0) → Ankunft **am eigenen Bett, egal wie weit weg**. Das ist ein Teleport ohne Tod, also **ohne Inventarverlust**, und ohne Server-Cooldown. Grenze: Es gibt nur **ein** Ziel (der gesetzte Respawnpunkt).

### 1.3 6b6t

| Punkt | Stand | Quelle |
|---|---|---|
| Ausgangsportal im End | „Fox rebuilds it: bedrock pillar and rim, end stone floor, torches, and portal blocks once the dragon has been killed.“ Abbau der Mitte (9×9) ist gesperrt. **Zuverlässig vorhanden.** | [Issue #16](https://github.com/6b6t/feedback/issues/16) |
| Natürliche Stronghold-Portale | unzerstörbar, aber laut Scan nur bei **79 von 128** gelisteten Strongholds noch ein Raum, **keiner in Ring 1–2**; manche Räume haben nur noch 2, 9 oder 11 Rahmen | [Issue #12](https://github.com/6b6t/feedback/issues/12) (Kommentar), [Issue #16](https://github.com/6b6t/feedback/issues/16) |
| Ringabstände (Vanilla) | Ring 1: 1.280–2.816, Ring 3: 7.424–8.960 … Ring 8: 22.784–24.320 Blöcke vom Ursprung | [Wiki: Stronghold](https://minecraft.wiki/w/Stronghold) |
| Von Spielern gebaute End-Portale | Scan ±50.442: **993** Strukturen, davon **311 mit Portalblöcken**, **57 innerhalb von 1.000 Blöcken um 0,0**, 97 auf y ≥ 256. Der Betreiber erwägt, sie zu entfernen (Issue offen) | [Issue #12](https://github.com/6b6t/feedback/issues/12) |
| End-Gateways in der Overworld | 0 gefunden (erwartet, es gibt sie nur im End) | [Issue #12](https://github.com/6b6t/feedback/issues/12) |
| End-Spawn | Wasserwürfel 1.400 × 1.400 um den End-Spawn (Piratas, Feb–März 2026) | [6b.wiki: Spawn](https://6b.wiki/Wiki/Spawn) (nicht offiziell) |
| End-Highway | „Construction of the end highway by The Highway Men“ (Bildunterschrift) | [Blog: THM](https://blog.6b6t.org/the-group-behind-the-6b6t-highways-the-highway-men/) |
| Bett-Spawnpunkte | Fehler „randomly cleared“ laut Betreiber in Fox `6014011` behoben, aktiv nach Neustart | [Issue #2](https://github.com/6b6t/feedback/issues/2) |
| Ankunft weit draußen | Nach Teleports in selten besuchte Gebiete (15.000 bis 5.000.000 Blöcke) wird man manchmal ≈ 5 s auf den Backup-Server geworfen. Offen | [Issue #28](https://github.com/6b6t/feedback/issues/28) |
| Offizielle Aussage zu Gateways | keine (Website, Blog, Regeln, Issues durchsucht) | – |

**Urteil:** Der End-Bett-Sprung funktioniert auf 6b6t nach allem, was offiziell belegt ist (Ausgangsportal garantiert, Bett-Bug behoben). End-Portale gibt es aber nur nahe am Spawn (Strongholds bis ≈ 24.000 Blöcke, Spielerportale im gescannten ±50.000-Quadrat). Praktisch ist er also ein **cooldown-freier Weg vom Spawn-Bereich zum eigenen Bett**, mit Inventar. Unsicher: Spielerportale können entfernt werden (#12), Portale nahe 0,0 können Fallen sein ([Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/): „it could be a trap“), der Wasserwürfel am End-Spawn bremst. *Nicht im Spiel getestet.*

---

## 2. Netherportal-Netze

### 2.1 Spielcode

| Punkt | Wert | Fundstelle |
|---|---|---|
| Zielkoordinate | Position × `DimensionType.getTeleportationScale` (Overworld→Nether ÷ 8, zurück × 8), danach `WorldBorder.clampToBounds` | `NetherPortalBlock.getPortalDestination` |
| Suchradius nach bestehendem Portal | **16 Blöcke im Nether**, **128 Blöcke in der Overworld** (Quadrat über POI `NETHER_PORTAL`, nächstes nach Abstand gewinnt) | `PortalForcer`: `NETHER_PORTAL_RADIUS = 16`, `OVERWORLD_PORTAL_RADIUS = 128`, `findClosestPortalPosition` |
| Neues Portal | wird im Umkreis 16 (Spirale, `BlockPos.spiralAround(…, 16, …)`) gesucht. Ohne Platz: erzwungen auf mindestens y 70 mit Obsidian-Plattform | `PortalForcer.createPortal` (`bipush 16`, `bipush 70`) |
| Wartezeit im Portal | Spielregel `players_nether_portal_default_delay` = **80 Ticks (4 s)** im Überleben | `GameRules`, `NetherPortalBlock.getPortalTransitionTime` |

### 2.2 Ein Portalnetz bauen (hergeleitet aus 2.1)

- Für ein Ziel bei Overworld (X, Z) baut man im Nether ein Portal bei (X/8, Z/8), höchstens ±16 Blöcke daneben. Dann verbindet die Overworld-Seite mit dem nächsten Portal im 128er-Umkreis um (X, Z).
- Zwei Overworld-Portale, die näher als ≈ 128 Blöcke beieinander liegen, können auf dasselbe Nether-Portal zeigen und umgekehrt (Nether-Portale < 16 Blöcke Abstand). Für ein Netz mit getrennten Zielen: Overworld-Portale > 128 Blöcke auseinander, die Nether-Seite exakt bei ÷ 8.
- Geschwindigkeit: Das Netz bringt nur den bekannten Faktor 8 ([schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) 2.3) plus 2 × 4 s Portalwartezeit pro Fahrt. **Kein eigener Geschwindigkeitsgewinn**, nur Komfort (feste Ausgänge an Basen).

### 2.3 6b6t

- Nether-Highways bis an die Weltgrenze sind belegt (siehe [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) Abschnitt 3). Laut Community-Wiki erreicht der letzte Diagonal-Tunnel 3,75 Mio. Nether-Blöcke, also die Overworld-Weltgrenze bei 30 Mio. ([6b.wiki: THM](https://6b.wiki/Wiki/The_Highway_Men), nicht offiziell).
- Portale über Worker-Grenzen hinweg laufen in Fox über eigene Hooks (`CrossServerPortalHooks` wird in [Issue #28](https://github.com/6b6t/feedback/issues/28) zitiert). Portale funktionieren also, aber ein Portal-Ausgang weit draußen kann denselben Backup-Kick wie `/home` auslösen (#28). *Ableitung, für Portale nicht getestet.*
- Offizielle Aussagen zu Portalnetzen oder Linking: keine gefunden.

---

## 3. Experimentelle Loren (`minecart_improvements`)

### 3.1 Spielcode

| Punkt | Wert | Fundstelle |
|---|---|---|
| Feature-Flag | `minecraft:minecart_improvements`, eingeschaltet nur durch das eingebaute Datapack `minecart_improvements` | `FeatureFlags.MINECART_IMPROVEMENTS`, `datapacks/minecart_improvements/pack.mcmeta` |
| Umschaltung | `AbstractMinecart.useExperimentalMovement(level)` = `level.enabledFeatures().contains(MINECART_IMPROVEMENTS)` → `NewMinecartBehavior`, sonst `OldMinecartBehavior` | `AbstractMinecart` |
| Spielregel | `max_minecart_speed`: Standard **8**, Bereich **1–1000** (Blöcke/s), nur mit dem Flag vorhanden | `GameRules`: `bipush 8`, `iconst_1`, `sipush 1000`, `FeatureFlagSet.of(MINECART_IMPROVEMENTS)` |
| Höchsttempo neu | `max_minecart_speed × (im Wasser 0,5 : 1) / 20` Blöcke/Tick | `NewMinecartBehavior.getMaxSpeed` |
| Antrieb neu | Antriebsschiene: `v + 0.06` pro Tick (ab 0,01), aus dem Stand `0.2` | `NewMinecartBehavior.calculateBoostTrackSpeed` |
| Dämpfung | mit Fahrgast 0,997, leer 0,975 (neu) bzw. 0,96 (alt) | `getSlowdownFactor` |
| Höchsttempo alt | **0,4 Blöcke/Tick = 8 Blöcke/s**, im Wasser 0,2 | `OldMinecartBehavior.getMaxSpeed` |

**Hergeleitet:** Mit Standardwert 8 ist die neue Lore genauso schnell wie die alte (8 Blöcke/s, Nether ×8 = 64 Blöcke/s). Nur wenn der Betreiber das Flag **und** einen hohen Wert setzt, wäre bis 1000 Blöcke/s (Nether: 8.000 Overworld-Blöcke/s) möglich. Feature-Flags werden in Vanilla beim Erstellen der Welt gewählt; die 6b6t-Welt stammt von 2022 (1.12.2) und wurde mehrfach konvertiert ([History](https://6b6t.org/en/history), [Issue #12](https://github.com/6b6t/feedback/issues/12): „The world was probably converted from an older format“).

### 3.2 6b6t

- **Keine offizielle Quelle** erwähnt Loren, Schienen, Feature-Flags oder `max_minecart_speed` (Website, Blog, Updates, alle Issues durchsucht).
- In [Issue #8](https://github.com/6b6t/feedback/issues/8) steht zur Live-Konfiguration: „There are no datapacks“. Da das Flag nur über das Datapack `minecart_improvements` kommt, ist es **sehr wahrscheinlich aus**. *Schluss aus dem Zitat; die Aussage bezog sich auf Phantome.*
- Der NCP-Fork hat für Loren nur Schienen-Sonderfälle (`VehicleEnvelope`: `canRails`, `MagicVehicle.maxRailsVertical = 0.5`), keinen eigenen horizontalen Grenzwert in Blöcken/s.
- **Prüfbar im Client:** Der Server schickt die aktiven Flags (`ClientboundUpdateEnabledFeaturesPacket`). Ein Modul kann nach dem Join `level.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS)` loggen. *Vorschlag, nicht umgesetzt.*

**Urteil:** Für 6b6t unbrauchbar. Ohne Flag 64 Overworld-Blöcke/s im Nether mit teurer Schienenstrecke, also langsamer als ein Pferd auf dem Highway (117).

---

## 4. Dreizack mit Riptide plus Elytra im Regen

### 4.1 Spielcode

- `TridentItem.releaseUsing`: mindestens **10 Ticks** laden (`THROW_THRESHOLD_TIME = 10`), Bedingung nur `isInWaterOrRain()` und **nicht** `isPassenger()`. **Keine** Prüfung auf `isFallFlying`. Wirkung: `player.push(look × Stärke)`, also Addition zur aktuellen Geschwindigkeit, dazu `startAutoSpinAttack(20, 8.0f, …)`.
- Stärke aus `riptide.json`: `1.5 + 0.75·(Stufe−1)` → Riptide III = **3,0 Blöcke/Tick** pro Wurf.
- Haltbarkeit: Dreizack `durability(250)` (`Items.TRIDENT`), 1 Punkt pro Wurf (`hurtWithoutBreaking(1, …)`). Steht er bei 1, geht kein Wurf mehr (`nextDamageWillBreak`). Unbreaking für Nicht-Rüstung: Schadensentfall `L/(L+1)` (`unbreaking.json`), mit III also im Mittel **1.000 Würfe**.
- Regen: `Entity.isInRain` → `Level.isRainingAt` → nur bei Niederschlag `RAIN`. Die Biomtemperatur sinkt ab y 80 um 0,05/40 = 0,00125 pro Block (`Biome.getHeightAdjustedTemperature`: `bipush 17` über Meereshöhe, `0.05f`, `40.0f`). In kalten Biomen schneit es oben, dann geht Riptide nicht. **Kein Regen im Nether und End**, daher kein Faktor 8.
- Wiki: „A Riptide trident can be used to propel a player wearing elytra if in rainy weather or in a body of water. This allows players to reach speeds upward of 500 blocks per second“ ([Wiki: Riptide](https://minecraft.wiki/w/Riptide)).

### 4.2 Simuliert

Eigenes Skript mit der Elytra-Formel aus [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) 2.2 (Schwerkraft 0,08, Luftwiderstand 0,99/0,98/0,99), alle *n* Ticks ein Riptide-III-Stoß von 3,0 in Blickrichtung. Mittel über 200 Ticks im Gleichgewicht:

| Wurf alle | Pitch 0° | Pitch +10° (leicht sinkend) | Pitch −10° (steigend) |
|---|---|---|---|
| 10 Ticks | **624 Blöcke/s** horizontal, −3 vertikal | 696 / −11 | 359 / **+427** |
| 11 Ticks (realistisch: 10 laden + 1 neu ansetzen) | **570 Blöcke/s**, −3 vertikal | 636 / −11 | 326 / +386 |
| 12 Ticks | 526 Blöcke/s | 586 / −10 | 300 / +353 |

Grob: Gleichgewicht `v ≈ 3 / (1 − 0,99ⁿ)` Blöcke/Tick, bei n = 11 also ≈ 28,5 Blöcke/Tick. Das passt zu „upward of 500 blocks per second“ im Wiki. *Simuliert, nicht im Spiel gemessen.*

Verbrauch bei 570 Blöcke/s (hergeleitet): 1,8 Würfe/s. Ein Dreizack mit Unbreaking III (≈ 1.000 Würfe) hält ≈ 550 s ≈ **313.000 Blöcke**. Eine Elytra ohne Unbreaking (432 s Flug) hält ≈ **246.000 Blöcke**. Eine Regenphase dauert 10–20 min ([Wiki: Weather](https://minecraft.wiki/w/Weather): „12000 to 24000 ticks“), also 340.000–680.000 Blöcke pro Regen. Mit Klar-Phasen von 10 min bis 2,5 h regnet es im Mittel ≈ 16 % der Zeit (18.000 / (18.000 + 96.000) Ticks, *hergeleitet*).

### 4.3 6b6t

- **Offiziell:** keine Aussage zu Riptide oder Dreizack (Website, Blog, Regeln, Issues). Riptide ist nicht verboten; Bewegungs-Hacks sind sogar erlaubt ([6b6t-regeln-limits.md](6b6t-regeln-limits.md)).
- **Anticheat-Code (NCP-Fork, öffentlich):** Während Riptide und bis 1.500 ms danach nutzt NCP ein eigenes Bewegungsmodell `riptiding` (`MovingConfig`: `data.timeRiptiding + 1500 > now`). Dessen Standardwerte: horizontal **700 %** von `ModelFlying.HORIZONTAL_SPEED = 0.6`, also 4,2 Blöcke/Tick; bei „Ripglide“ (Riptide + Elytra) kommen `+ 9.3` dazu (`CreativeFly`, Tag `hripglide`), also ≈ **13,5 Blöcke/Tick ≈ 270 Blöcke/s** (`DefaultConfig`, Zeilen 501–505). Ein Code-Kommentar nennt „Riptide + gliding (+ firework !?)“ ausdrücklich als „unhealthy“ Bewegung, die schlecht für das Chunk-Laden ist (`MovingListener.checkExtremeMove`).
- **Hergeleitet:** Vanilla-Ripglide (≈ 28 Blöcke/Tick) liegt etwa doppelt über diesem Standard-Grenzwert. Mit Standardkonfiguration wären Setbacks oberhalb von ≈ 270 Blöcke/s zu erwarten. Die **Live-Konfiguration ist nicht veröffentlicht**; ob 6b6t andere Werte nutzt, ist unbekannt. Laut Betreiber ist die Politik für hohe Geschwindigkeiten noch offen ([Issue #4](https://github.com/6b6t/feedback/issues/4)).
- **Chunks:** 570 Blöcke/s sind ≈ 36 Chunks/s. Schon bei Nether-Elytra (≈ 33 Blöcke/s) stoppte die Generierung neuer Chunks nach ≈ 1.000 Blöcken ([Issue #1](https://github.com/6b6t/feedback/issues/1), geschlossen). In alten, bereits generierten Gebieten ist es weniger kritisch, aber unbelegt.
- **Wetter auf Folia:** Ein Spieler meldet, dass Tag/Nacht zwischen den Sub-Servern nicht synchron ist ([Issue #8](https://github.com/6b6t/feedback/issues/8), Spielerzitat). Ob das Wetter pro Worker unterschiedlich ist, steht nirgends.

**Urteil:** Theoretisch das schnellste Vanilla-Mittel in der Overworld (≈ 570 Blöcke/s, Nether-Elytra nur 268). Auf 6b6t **unzuverlässig**: nur bei Regen (≈ 16 % der Zeit, nicht über kalten Biomen), Grenzwert im Anticheat vermutlich ≈ 270 Blöcke/s, Chunk-Laden unklar. *Nicht auf 6b6t getestet.*

---

## 5. Happy Ghast

Gibt es in 26.2 (`net.minecraft.world.entity.animal.happyghast.HappyGhast`, Spawn-Ei, Geschirr-Texturen im Jar). Tempo ≈ 3,5 Blöcke/s, Details in [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) 2.5. Im Nether ×8 wären das ≈ 28 Overworld-Blöcke/s (*hergeleitet*), also langsamer als Sprinten auf dem Highway (45). 6b6t erwähnt den Happy Ghast in keiner offiziellen Quelle. **Für Langstrecken unbrauchbar.**

---

## 6. Respawn-Tricks

### 6.1 Spielcode

| Punkt | Wert | Fundstelle |
|---|---|---|
| Respawn ohne Bett | `TeleportTransition.createDefault` → `ServerPlayer.adjustSpawnLocation` → `PlayerSpawnFinder.findSpawn` | Bytecode |
| Streuung | Spielregel `respawn_radius`, Standard **10**, begrenzt durch den Abstand zur Weltgrenze. Kandidaten im Quadrat (2r+1)², höchstens 1.024 Versuche | `GameRules` (`bipush 10`), `PlayerSpawnFinder` (`ABSOLUTE_MAX_ATTEMPTS = 1024`, `WorldBorder.getDistanceToBorder`) |
| Erster Join | *nicht geprüft*, ob derselbe Weg genutzt wird | – |
| Bett/Anker beim Tod | `findRespawnPositionAndUseSpawnBlock(true, …)`: Anker verliert 1 Ladung. Über das End-Portal (`false`) nicht (1.2) | `ServerPlayer.findRespawnAndUseSpawnBlock` |
| Seelenanker | nur im Nether nutzbar (`RespawnAnchorBlock.canSetSpawn`), Respawn direkt im Nether | Bytecode |
| Sofort-Respawn | Spielregel `immediate_respawn` existiert, Standard aus | `GameRules` |

### 6.2 6b6t

| Punkt | Stand | Quelle |
|---|---|---|
| Ohne Bett | „If you die without a bed, you start at spawn again, but /home works there.“ | [Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/) |
| Streuung | **Viel größer als Vanilla 10**, Wert nicht veröffentlicht. Blog: „/kill does the same as jumping: you die and respawn somewhere new“ und „You will most likely have to jump down many times“. Community-Studie mit 49.715 Respawns: Mittelpunkt bei (−450, −434), innerhalb der Zone gleichmäßig verteilt | [Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/), [6b.wiki: Spawn](https://6b.wiki/Wiki/Spawn) (nicht offiziell) |
| Spawn-Zone | `/tpa`-Sperre bei \|X\| oder \|Z\| ≤ 5.000 (Nether 2.000); Blog nennt „5000 x 5000 spawn area“; Obsidian-Dach ≈ 7k × 7k (Community) | [Blog](https://blog.6b6t.org/how-to-escape-the-6b6t-spawn/), [Geburtstag](https://blog.6b6t.org/a-special-day-for-6b6t-3rd-birthday/), [6b.wiki](https://6b.wiki/Wiki/Spawn) |
| `keep_inventory` | nicht veröffentlicht | – |
| Bett-Bug | behoben, siehe 1.3 | [Issue #2](https://github.com/6b6t/feedback/issues/2) |
| Home-Netz | siehe [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) 1.2 und 5 | – |

### 6.3 Was daraus folgt (hergeleitet)

1. **End-Bett-Sprung statt `/kill`:** gleiches Ziel (Bett), aber **ohne Tod und mit Inventar**, ohne Cooldown (Abschnitt 1.2/1.3). Braucht ein erreichbares End-Portal, also praktisch nur rund um den Spawn.
2. **Seelenanker im Nether als Respawnpunkt:** `/kill` oder End-Sprung landen direkt im Nether, z. B. an einem Highway-Ende. Kombiniert mit Nether-Elytra (×8). Beim Tod kostet es 1 Ladung, über das End-Portal nicht.
3. **Respawn-Lotterie am Spawn:** bringt nur Punkte innerhalb der Spawn-Zone (±5.000), also keine Fernreise.
4. Pro Spieler gibt es nur **einen** Respawnpunkt. Ein „Bett-Netz“ geht nur, indem man vor der Abreise das Bett am Ziel neu setzt; ein echtes Netz mit vielen Zielen bleibt `/home`.

---

## 7. Was nicht gefunden wurde

- Keine offizielle 6b6t-Aussage zu End-Gateways, End-Portal-Reisen, Loren, Feature-Flags, Riptide oder Happy Ghast.
- Kein veröffentlichter Wert für die Respawn-Streuung (`respawn_radius` oder Plugin) und kein Nachweis, ob `keep_inventory` aus ist.
- Die Live-Konfiguration von NoCheatPlus ist nicht öffentlich. Die 270 Blöcke/s für Ripglide sind aus den **Standardwerten** des Forks abgeleitet.
- Ob `minecart_improvements` aktiv ist, ist nicht direkt belegt (nur „There are no datapacks“ in #8).
- Wie viele End-Gateways im 6b6t-End existieren (Zahl der Drachentode), ist unbekannt.
- Keine Messung im Spiel. Riptide-Elytra simuliert, alles andere aus Code-Konstanten hergeleitet.

---

## Rangliste nach Overworld-Blöcken pro Sekunde

Teleports hängen nicht von der Strecke ab. Für sie steht die Gesamtzeit, die Blöcke/s ergeben sich daraus (1 Mio. Blöcke in 60 s ≈ 16.700 Blöcke/s). *Alle Zeiten hergeleitet; Werte aus [schnell-reisen-6b6t.md](schnell-reisen-6b6t.md) sind mit (S) markiert.*

| Rang | Methode | Overworld-Blöcke/s | 100.000 Blöcke | 1.000.000 Blöcke | Zuverlässigkeit auf 6b6t |
|---|---|---|---|---|---|
| 1 | **`/home`** (S) | streckenunabhängig | Cooldown + 3–15 s Ausführung | gleich | hoch; nur zu eigenen Homes, Backup-Kick möglich (#28) |
| 2 | **End-Bett-Sprung** (End-Portal → End → Ausgangsportal → Bett) | streckenunabhängig | ≈ 1–3 min ab einem End-Portal (≈ 100 Blöcke im End, Portale ohne Wartezeit) | gleich | mittel; nur ein Ziel, End-Portale nur nahe Spawn, Spielerportale evtl. entfernt (#12), Ausgangsportal garantiert (#16) |
| 3 | Bett + `/kill` (S) | streckenunabhängig | Sekunden | gleich | mittel; **kostet das Inventar** |
| – | Experimentelle Lore im Nether, `max_minecart_speed` 1000 | 8.000 (Theorie) | 12,5 s | 2,1 min | **nicht verfügbar** (Flag sehr wahrscheinlich aus, Standard 8) |
| 4 | Boot auf Blaueis im Nether (S) | 582 | 2,9 min | 29 min | sehr niedrig; keine Eisbahnen belegt |
| 5 | **Riptide III + Elytra im Regen** (Overworld) | ≈ 570 (simuliert); ≈ 270 bei NCP-Standardgrenze | 2,9 min (6,2 min bei 270) | 29 min (62 min) | niedrig; nur Regen (≈ 16 %), nicht über kalten Biomen, Anticheat-Grenze und Chunk-Laden unklar |
| 6 | Elytra + Raketen im Nether (S) | 268 | 6,2 min | 62 min | mittel–hoch; Standard auf den Highways |
| 7 | Pferd im Nether (S) | 117 | 14 min | 2,4 h | hoch |
| 8 | Lore im Nether (alt oder neu mit Standard 8) | 64 | 26 min | 4,3 h | niedrig; Schienen müssten gebaut werden |
| 9 | Sprinten im Nether (S) | 45 | 37 min | 6,2 h | hoch |
| 10 | Happy Ghast im Nether | ≈ 28 | 60 min | 10 h | hoch, aber sinnlos |
| – | End-Gateway | 0 (nur innerhalb des Ends, ≈ 1.000–1.300 Blöcke pro Sprung) | – | – | – |
| – | Netherportal-Netz | Faktor 8 wie oben, + 2 × 4 s pro Fahrt | – | – | hoch |

**Kurz:** Für Fernreisen bleibt die Kombination aus einem streckenunabhängigen Sprung (`/home`, End-Bett-Sprung) und Nether-Elytra für den Rest am verlässlichsten. Riptide-Elytra ist auf dem Papier doppelt so schnell wie Nether-Elytra, hängt aber an Regen und an einer unveröffentlichten Anticheat-Grenze.
