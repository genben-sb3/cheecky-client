# Perlenkanone ohne Duper für 10.000–100.000+ Blöcke (MC 1.21.x / 26.2 / 26.3, 6b6t) – Recherche

Stand: 2026-10-27. Ziel: herausfinden, welche Perlenkanonen **ohne jede Art von Duping** (kein TNT-, Teppich- oder Schienen-Duper) sehr große Weiten schaffen, also 10.000 bis 100.000+ Blöcke, und was ein Schuss jeweils kostet. Ziel-Server ist 6b6t (Folia-Fork „Fox“, Version 26.2, Paper-Standard `allow-piston-duplication: false`).
**Nicht im Umfang:** Anti-Cheat-Umgehung, Exploits, Dupes. Was schon in [perlenkanone.md](perlenkanone.md) (Explosionsphysik, Perlenflug, kleine Spender-Kanone), [bean-blaster.md](bean-blaster.md) (Windkugel-Kanone), [tnt-duper.md](tnt-duper.md) und [6b6t-regeln-limits.md](6b6t-regeln-limits.md) steht, wird hier nur verlinkt.

Alle Quellen wurden am **2026-10-27** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Spiel-Jar 26.2 (`minecraft-merged-201bed67e3-26.2.jar`) aus `.gradle/loom-cache`, gelesen mit `javap -c -p`; Datenpakete im selben Jar (`trial_spawner/…/breeze/*.json`, `loot_table/…`, `recipe/…`) | Spielcode | Primärquelle |
| Paper-Quellcode, Branch `main`, Commit `9d95051` (2026-10-27): `SpigotWorldConfig.java`, `WorldConfiguration.java`, Patches für `PrimedTnt`, `Projectile`, `ThrownEnderpearl`, `BlockGetter`, `ServerLevel` | Server-Code | Primärquelle |
| Folia-Quellcode, Branch `ver/26.2.x`, Commit `acf6733` (2026-09-22): `0001-Region-Threading-Base.patch` (minecraft- und paper-patches) | Server-Code | Primärquelle |
| Video „This small machine sends you MILLIONS of blocks... Breeze wind charge ender pearl cannon launcher“ von **Rays Works** ([gbumI1JYt94](https://www.youtube.com/watch?v=gbumI1JYt94), 2025-10-25, „For java 1.21.8+“) | Video des Erfinders | Primärquelle. **Automatisches Transkript** und Beschreibung gelesen, Bild nicht gesehen |
| Video „Breeze Powered Ender Pearl Cannon + Bulk Static Breeze Charge Generator V2 1.21.10“ von **CubeControl** ([xb_QcU_O8Uo](https://www.youtube.com/watch?v=xb_QcU_O8Uo), 2025-10-14) | Video des Erfinders | Nur die Beschreibung gelesen (kein Transkript vorhanden) |
| Video „Wind Charge Ender Pearl Cannon (NO TNT)“ von **Mitomon** ([Ob9F_r3u9_U](https://www.youtube.com/watch?v=Ob9F_r3u9_U), 2024-02-07, Snapshot 24w06a) | Video des Erfinders | Transkript gelesen |
| [minecraft.wiki: Tutorial:360 degree ender pearl cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon), [Trial Spawner](https://minecraft.wiki/w/Trial_Spawner), [Breeze](https://minecraft.wiki/w/Breeze), [Vault](https://minecraft.wiki/w/Vault), [Tutorial:Creeper farming](https://minecraft.wiki/w/Tutorial:Creeper_farming) | Wiki | Primärquelle (Community) |
| [github.com/6b6t/feedback](https://github.com/6b6t/feedback) Issues #1, #4, #7, #12, #16, #28 | Betreiber | offiziell (interne Arbeitsnotizen) |

---

## 1. Die harte Grenze: Wie viel Stoß braucht man?

### 1.1 Startgeschwindigkeit pro Weite

Eine Perle fliegt höchstens **99 Blöcke pro Block/Tick** Startgeschwindigkeit ([perlenkanone.md](perlenkanone.md) 1.6). Eigene Simulation mit der Tick-Reihenfolge aus `ThrowableProjectile.tick` (Schwerkraft 0,03 → Luftwiderstand 0,99 → Bewegung), bester Winkel, Landung auf Starthöhe:

| Weite (Oberwelt) | nötige Startgeschw. ohne Wasser | mit 1. Tick in Wasser (×0,8, z. B. Blasensäule) | bester Winkel | Flugzeit | Gipfelhöhe über Start |
|---|---|---|---|---|---|
| 10.000 | 102 b/t | 127 b/t | ≈ 7,7° | ≈ 28 s | ≈ 850 |
| 25.000 | 253 b/t | 314 b/t | ≈ 4° | ≈ 35 s | ≈ 1.200 |
| 50.000 | 506 b/t | 626 b/t | ≈ 2,5° | ≈ 42 s | ≈ 1.550 |
| 100.000 | **1.011 b/t** | **1.251 b/t** | ≈ 1,4° | ≈ 46–49 s | ≈ 1.800 |

*Hergeleitet (Skript), nicht im Spiel gemessen.* Die Gipfelhöhe liegt weit über der Bauhöhe; Entities dürfen dort fliegen. Wer waagerecht schießt (wie der Bean Blaster), braucht etwa 1,5–2× so viel Stoß wie beim besten Winkel ([bean-blaster.md](bean-blaster.md) 3.2).

### 1.2 Stoß pro Explosion ist auf etwa 1 b/t begrenzt

Aus `ServerExplosion.hurtEntities` ([perlenkanone.md](perlenkanone.md) 1.1): `Δv = (1 − d/(2·Stärke)) · exposure · knockbackMultiplier`. Der erste Faktor ist höchstens 1. **Eine stärkere Explosion schiebt also nicht stärker**, sie reicht nur weiter. Die einzigen Multiplikatoren ≠ 1 im Code 26.2 (alle Verwender von `SimpleExplosionDamageCalculator` per `grep` gesucht):

| Explosionsquelle | Stärke | Rückstoß-Multiplikator | max. Δv realistisch | Beleg |
|---|---|---|---|---|
| TNT (Spender) | 4 | 1,0 | 0,75–0,90 (Abstand ≥ 0,79) | [perlenkanone.md](perlenkanone.md) 1.5 |
| Windkugel (Item, Spender) | 1,2 | **1,22** | 0,53–0,62 im Bean Blaster, theoretisch ≤ ≈ 1,07 | `WindCharge`, [bean-blaster.md](bean-blaster.md) 3.1 |
| **Breeze-Windkugel** (von einer Breeze geschossen) | **3,0** | 1,0 (`AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR`, Rückstoß `Optional.empty()`) | ≈ 0,67 (aus den Angaben von Rays Works zurückgerechnet, siehe 2.2) | `BreezeWindCharge.explode`: `ldc 3.0f` |
| Ghast-Feuerball | 1 (`LargeFireball.explosionPower = 1`) | 1,0 | ≤ 1 | `LargeFireball` |
| End-Kristall | 6 | 1,0 | ≤ 1 | `EndCrystal.hurtServer` |
| Seelenanker / Bett | 5 | 1,0 | ≤ 1 | `RespawnAnchorBlock.explode`, `BedBlock` |
| Streitkolben „Windstoß“ | – | laut Datenpaket bis 2,2 | – | nur 1 Explosion pro Schlag, am Spieler; nicht stapelbar (nicht im Code geprüft) |

**Folge:** Für 100.000 Blöcke in der Oberwelt braucht man **≈ 1.000–1.900 Explosionen im selben Tick**, je nach Quelle und Wasser. Es gibt keinen vanilla-legitimen Weg, einen einzelnen Stoß größer zu machen. Die einzigen Hebel sind (a) **viele Explosionen gleichzeitig** und (b) der **Nether-Faktor 8** (Abschnitt 2.3).

### 1.3 Warum mehrere Explosionen nicht über mehrere Ticks gehen

Die Perle bewegt sich jeden Tick mit ihrer vollen Geschwindigkeit (`setPos(position + delta)`). Nach dem ersten Stoß ist sie im nächsten Tick 1–100+ Blöcke weg. Spätere Explosionen am Startpunkt treffen sie nicht mehr ([bean-blaster.md](bean-blaster.md) 3.1). Alle Explosionen müssen also **im selben Tick** stattfinden.

---

## 2. Bauweisen im Einzelnen

### 2.1 TNT aus Spendern (ohne Duper)

- **Mechanismus:** wie in [perlenkanone.md](perlenkanone.md) 2.2, nur mit sehr vielen Spendern, die im selben Tick zünden. Ein Spender gibt pro Puls 1 TNT aus.
- **Problem 1, TNT schiebt TNT:** Jede Explosion schiebt das restliche TNT, das sich vor dem eigenen Explodieren noch bewegt ([perlenkanone.md](perlenkanone.md) 1.5). Ab einigen Dutzend TNT braucht man exakte Ausrichtung (große 360°-Kanonen machen das mit „Alignern“, [Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).
- **Problem 2, Server-Limit (neu, Primärquelle):** Paper/Spigot hat in `spigot.yml` **`max-tnt-per-tick: 100`** (`SpigotWorldConfig.maxTntPerTick`, Standard 100). Im Paper-Patch für `PrimedTnt.tick` steht als erste Zeile: `if (maxTntTicksPerTick > 0 && ++currentPrimedTnt > maxTntTicksPerTick) { return; }`. TNT über der Grenze **tickt in diesem Tick gar nicht**, seine Zündschnur läuft also nicht weiter, und es explodiert später. **Folia** übernimmt das, zählt aber pro Region (`regionizedWorldData.currentPrimedTnt`, Folia-Patch Zeile 16407). *Hergeleitet:* Auf Paper/Folia mit Standardwert explodieren höchstens 100 TNT pro Tick und Region gemeinsam. Das sind ≈ 75–90 b/t, also **höchstens ≈ 7.000–8.800 Blöcke** (ohne bzw. mit Wasser im ersten Tick). Für 10.000+ reicht das nicht. Die 6b6t-Konfiguration ist nicht öffentlich.
- **Material pro Schuss:** 5 Schwarzpulver + 4 Sand pro TNT (`recipe/tnt.json`). Bau: pro TNT 1 Spender (7 Bruchstein, 1 Bogen, 1 Redstone).
- **Vanilla-legitim:** ja. Auf Paper/Folia aber durch das TNT-Limit gedeckelt.

### 2.2 Windkugeln aus Spendern (Bean Blaster und Varianten)

- **Mechanismus:** [bean-blaster.md](bean-blaster.md). Windkugeln schieben sich gegenseitig nicht (`AbstractWindCharge.push` ist leer) und kollidieren nicht miteinander. Deshalb können **beliebig viele** am selben Punkt im selben Tick explodieren. Das ist der entscheidende Vorteil gegenüber TNT.
- **Skalieren:** 576 Windkugeln pro Spender (9 × 64). Mehrere Spender pro Speicher sind nach der Mechanik möglich (*hergeleitet*), dann gibt es keine feste Obergrenze. Laden: ≈ 1 Windkugel alle 4 Ticks pro Spender ([bean-blaster.md](bean-blaster.md) 2.2).
- **Kein Server-Limit** für Windkugeln in Paper gefunden (`max-tnt-per-tick` gilt nur für `PrimedTnt`). Fox-`EntityLimits` für `wind_charge`: unbekannt.
- **Material:** 1 Breeze-Rute = 4 Windkugeln (`recipe/wind_charge.json`). Pro Schuss wird alles verbraucht.
- **Vanilla-legitim:** ja.

### 2.3 Breeze-Windkugeln aus einer gefangenen Breeze („Breeze-Kanone“) – **kostet pro Schuss nichts**

- **Erfinder/Belege:**
  - **Rays Works** ([Video](https://www.youtube.com/watch?v=gbumI1JYt94), Java 1.21.8+): Eine Breeze steht in einem Käfig, ein angeleinter **Eisengolem** im Gehege dient als Ziel, „so that the breeze will continuously shoot out breeze wind charges“ (0:51–1:04). Ein kalibrierter Sculk-Sensor erkennt jeden Schuss. Ein Spinnennetz nimmt den Windkugeln den Schwung, ein Honigblock am Kolben schiebt sie zur Seite in den Speicher (7:45–8:06, 8:40–8:50). Ein Spender bzw. Dropper mit Items zählt die Schüsse: „one stack of items … will send you 18,000 blocks … the whole thing full … over 160,000 blocks“ (11:41–11:59). Die Perle hängt zwischen Seelensand- und Magma-Blasensäule, Honigblöcke richten sie aus (3:12–3:55, 10:38–11:07). Laut Video trägt der Schuss die Perle durch ein **Netherportal direkt neben der Kammer** in den Nether (siehe 2.4).
  - **CubeControl** ([Video](https://www.youtube.com/watch?v=xb_QcU_O8Uo), 1.21.10): „bulk breeze charge generator“ mit **mehreren gestapelten Breezes**. Laut Beschreibung hat eine Kolben-Änderung in 1.21.10 bei Spinnennetz/Pulverschnee das Einsammeln vereinfacht. Inhalt nicht geprüft.
- **Code-Belege (26.2):**
  - `BreezeWindCharge extends AbstractWindCharge` → genau wie Item-Windkugeln: kein Schieben untereinander, also stapelbar. Explosion Stärke 3,0, Rückstoß ×1,0, also `Δv = (1 − d/6) · exposure` (`BreezeWindCharge.explode`).
  - Schuss-Takt der Breeze (`breeze.Shoot`, statische Konstanten): Aufladen **15 Ticks**, Erholen **4 Ticks**, Abklingzeit **10 Ticks**. *Hergeleitet:* etwa **1 Windkugel pro ≈ 29 Ticks (≈ 1,45 s)** pro Breeze, also ≈ 2.500/h, solange sie ein Ziel sieht. Reichweite ≤ 16 Blöcke (`ATTACK_RANGE_MAX_SQRT = 256`). `Breeze` referenziert `EntityTypes.IRON_GOLEM` (Zielwahl; nur grob geprüft).
- **Rückrechnung aus den Erfinder-Angaben:** 64 Windkugeln → 18.000 Oberwelt-Blöcke = 2.250 Nether-Blöcke, also ≈ 35 Nether-Blöcke pro Kugel. Bei waagerechtem Schuss von Y 255 auf das Netherdach (Fallhöhe 127, erster Tick in Wasser) ist der Weite-Faktor S = 53,2 (Simulation). Daraus folgt Δv ≈ **0,66 b/t pro Breeze-Kugel**. *Rückrechnung, nicht gemessen.* 576 Kugeln × 35 × 8 ≈ 161.000, das passt zu „over 160,000“.
- **Material pro Schuss: 0.** Einmalig: eine Breeze aus einer Trial Chamber holen (laut Video per Boot und Minecart, durch ein Netherportal), ein Eisengolem, Redstone, Sculk-Sensor, Kolben, Schleim, Honig. Ein Namensschild gegen Despawnen ist *naheliegend, aber nicht im Code geprüft*.
- **Grenzen:** Viele Windkugel-Entities laggen: „your Minecraft will probably crash … the more you have, the more strain“ (12:08–12:21). Rays Works empfiehlt, die Kugeln in ungeladene Chunks zu schieben (Beschreibung). Ladezeit mit 1 Breeze: ≈ 1,45 s pro Kugel.
- **Vorsicht, im selben Video gezeigte Exploits (nicht übernehmen):** Er kommt per „pearl … clip through“ durch 1 Block dickes Grundgestein auf das Netherdach (5:47–5:57). Ein „weird glitch that allows Ender Pearl to clip right through completely solid walls“ (7:01–7:09) und ein „Instant teleportation glitch“ (Kapitel 13:30) werden erwähnt. Das sind Bug-Ausnutzungen und nicht Teil dieser Empfehlung. Die eigentliche Kanone braucht sie nicht.

### 2.4 Nether-Faktor 8 (gilt für jede Kanone)

- **Mechanismus:** Ein Netherportal direkt vor der Perle. Die Perle fliegt durch und behält ihre Geschwindigkeit: `NetherPortalBlock` baut die `TeleportTransition` mit `Relative.union(DELTA, ROTATION)` (Bytecode, Zeilen 500–506). Die Zielkoordinate wird mit `DimensionType.getTeleportationScale` umgerechnet (Faktor 8). Jeder Block im Nether entspricht 8 Blöcken in der Oberwelt, sobald man am Ziel ein Portal zurück baut. Diesen Aufbau nutzt Rays Works (Transkript 3:56–4:14, 13:20–13:28).
- **Nötige Startgeschwindigkeit sinkt auf ⅛.** Simulation, Schuss vom Portal auf Y 255 über dem Netherdach, Landung auf dem Dach (Y 128), erster Tick in Wasser, bester Winkel:

  | Oberwelt-Äquivalent | Nether-Weite | nötige Startgeschw. | Flugzeit |
  |---|---|---|---|
  | 10.000 | 1.250 | 17,6 b/t | 14 s |
  | 25.000 | 3.125 | 41 b/t | 19 s |
  | 50.000 | 6.250 | 80 b/t | 25 s |
  | 100.000 | 12.500 | **158 b/t** | 30 s |
  | 200.000 | 25.000 | 314 b/t | 36 s |

- **Haken: das Netherdach.** Unter dem Dach (Y < 128) trifft eine lange, flache Flugbahn fast sicher Gelände. Über 1.000+ Blöcke geht es praktisch nur **über dem Dach**. In Vanilla kommt man dort nur per Bug hin (Perlen-Clip, Grundgestein brechen). **Auf 6b6t ist das anders:** Fox hat ein eingebautes **„Bedrock breaking“**: „A player who keeps mining an unbreakable block for `bedrock-breaking-interval` seconds within 10 blocks turns it into air“ ([Issue #12](https://github.com/6b6t/feedback/issues/12), [#16](https://github.com/6b6t/feedback/issues/16)). Das ist dort eine Server-Funktion und kein Exploit. Damit ist das Dach auf 6b6t legitim erreichbar. Ob 6b6t Paper's `environment.nether-ceiling-void-damage-height` (Standard: aus, `WorldConfiguration` Zeile 449) eingeschaltet hat, ist **unbekannt**.
- **Am Ziel:** Man landet auf dem Netherdach bei (X/8, Z/8) und baut dort ein Portal (Obsidian + Feuerzeug, also 10–14 Obsidian pro Ziel). Rays Works nennt als Alternative: Steht am Ziel schon ein Portal auf dem Dach, fliegt die Perle hindurch, und man landet direkt in der Oberwelt (13:30–13:41).

### 2.5 Geprüft und verworfen (skaliert nicht)

| Idee | Ergebnis | Beleg |
|---|---|---|
| **End-Kristalle** | Ein Kristall, den eine Explosion trifft, wird **ohne eigene Explosion** entfernt: `EndCrystal.hurtServer` → `remove(KILLED)`, explodiert nur bei `!source.is(IS_EXPLOSION)`. Außerdem nimmt jedes Entity im Umkreis 2·Stärke **mindestens 1 Schaden**, auch wenn es verdeckt ist (`ExplosionDamageCalculator.getEntityDamageAmount`: `… + 1.0`). *Hergeleitet:* Die erste Kristall-Explosion löscht alle anderen Kristalle im Umkreis von 12 Blöcken. Mehr als ≈ 1 wirksamer Kristall pro Schuss ist nicht drin. Dispenser können Kristalle nicht setzen (`EndCrystalItem` ist kein `ProjectileItem`, kein eigenes Spender-Verhalten). Kosten wären 1 Ghast-Träne + 1 Enderauge + 7 Glas pro Kristall (`recipe/end_crystal.json`). | Bytecode 26.2 |
| **Seelenanker / Betten** | Explodieren nur durch Spieler-Interaktion (`useWithoutItem`), nicht durch Redstone oder Explosionen. Jeder Klick ist eine Explosion, Stärke 5, die den Spieler selbst trifft. Mehrere im selben Tick nur per schneller Klick-Folge, mit Todesgefahr; praktisch wertlos. | `RespawnAnchorBlock`, `BedBlock` |
| **Ghast-Feuerbälle** | `AbstractHurtingProjectile` überschreibt `push` nicht. Feuerbälle werden also von anderen Explosionen weggeschoben und sind nicht stapelbar wie Windkugeln. Spender schießen nur kleine Feuerbälle (ohne Explosion). Zerstören Blöcke und setzen Feuer. | Bytecode |
| **Kolben/Schleim, Blasensäule** | Schleim setzt die Geschwindigkeit auf genau ±1 b/t (nicht additiv), Blasensäule max. 0,7 b/t nach oben. | [bean-blaster.md](bean-blaster.md) 2.2 |
| **Perle in Minecart/Boot** | Projektile sind nicht `isPushable` (`Entity.isPushable` = false, nicht überschrieben). Boote und Minecarts nehmen sie nicht auf. | Bytecode |
| **Wurf aus Elytra-Flug / Fahrzeug** | Die Perle erbt die Bewegung des Werfers: `Projectile.shootFromRotation` addiert `getKnownMovement()` (beim `ServerPlayer` die letzte Client-Bewegung bzw. die des Fahrzeugs). Paper kann das mit `misc.disable-relative-projectile-velocity` abschalten (Standard: aus). Elytra-Sturzflug ≈ 3,9 b/t ([Issue #4](https://github.com/6b6t/feedback/issues/4)) + Wurf 1,5 → *hergeleitet* höchstens ≈ 500 Blöcke. Ripglide (≈ 28 b/t) wäre ≈ 2.800 Blöcke, wird aber vom Anticheat gebremst ([ultraschnell-reisen-6b6t.md](ultraschnell-reisen-6b6t.md)). | Bytecode, Paper-Patch |
| **Perle unterwegs nochmal beschleunigen** | Theoretisch möglich, weil die Flugbahn deterministisch ist. Bei 100+ b/t liegen die Tick-Positionen aber 100+ Blöcke auseinander, und die zweite Station müsste im exakt richtigen Tick auf ≤ 2–8 Blöcke getroffen werden, in geladenen Chunks. **Keine Quelle, kein bekannter Bau.** Nicht praktikabel. | – |
| **Breeze-Windkugeln direkt (ohne Speicher)** | Eine Breeze schießt nur auf Ziele, nicht auf Befehl. Ohne Speicher immer nur 1 Kugel. | `breeze.Shoot` |
| **Spender schießt Perle** | Geht nicht: `EnderpearlItem` ist kein `ProjectileItem`, der Spender wirft sie nur als Item aus. | Bytecode |

### 2.6 Staffel („Perlen-Autobahn“)

Mehrere fest gebaute Kanonen entlang einer Route, jede mit eigener Perle. Jede Etappe kostet einen eigenen Schuss und einen Neustart (Perle einwerfen, warten). Das umgeht die Speed-Grenzen aus Abschnitt 3, weil jede Etappe kurz bleibt. Lohnt sich nur für eine feste Route, die man oft fährt. *Hergeleitet, kein dokumentierter Bau gefunden.*

---

## 3. Server- und Engine-Grenzen

| Grenze | Wirkung | Beleg |
|---|---|---|
| Keine Geschwindigkeitsgrenze für Entities | Vanilla: `Entity.push` prüft nur `isFinite`. In Paper/Folia gibt es keine Option „max entity speed“ (`WorldConfiguration` durchsucht). | [perlenkanone.md](perlenkanone.md) 1.1, Paper-Code |
| Chunk-Laden durch die Perle (seit 1.21.2) | Ticket bei jedem neuen Chunk, Radius 2. Paper/Moonrise ergänzt ein dauerhaftes `ENDER_PEARL_TICKER`-Ticket, weil das Vanilla-Ticket abläuft, bevor neue Chunks fertig generiert sind („Fixes MC-297591“, Kommentar im Folia-Patch). Eine schnelle Perle wartet in jedem neuen Chunk, bis er geladen ist. Bei 12.500 Nether-Blöcken werden *grob* 4.000 Chunks erzeugt. | [perlenkanone.md](perlenkanone.md) 3.1, Folia-Patch Zeile 175 ff. |
| **Paper: Raytrace durch ungeladene Chunks** | Paper-Patch „Prevent raytrace from loading chunks“ in `BlockGetter.clip`: Ist ein Block nicht geladen, gibt der Strahl sofort **MISS** zurück. *Hergeleitet:* Eine sehr schnelle Perle fliegt auf Paper durch noch nicht geladenes Gelände hindurch (Berge, Bäume) und landet später womöglich in einem Block. In Vanilla lädt `getBlockState` den Chunk dagegen synchron (Lag). | Paper-Patch `BlockGetter.java.patch` |
| `max-tnt-per-tick` (Paper/Spigot 100, Folia pro Region) | Deckelt TNT-Kanonen auf ≈ 7.000–8.800 Blöcke (2.1). Windkugeln sind nicht betroffen. | `SpigotWorldConfig`, `PrimedTnt.java.patch` |
| **Folia: Projektil tickt nicht außerhalb der eigenen Region** | `ThrowableProjectile.tick` beginnt in Folia mit `if (!TickThread.isTickThreadFor(level, position(), getDeltaMovement(), 1)) return;`. Geprüft wird jedes Regionsfeld (Standard `grid-exponent: 4` = 16 × 16 Chunks = 256 Blöcke) im Rechteck von der Position bis Position + Geschwindigkeit, ±1 Chunk. Gehört eines nicht zur aktuellen Region, **tickt die Perle nicht** und bleibt stehen. Leere Felder gehören nur im Abstand 1 zu geladenen Chunks zur Region (`ThreadedRegionizer`-Parameter `emptySectionCreateRadius = 1`). *Hergeleitet:* Sicher sind nur ≈ **240 b/t pro Achse** (bestenfalls ≈ 495). Darüber friert die Perle ein, wenn vor ihr keine anderen geladenen Chunks liegen. Das sind ≈ 19.000 Blöcke Direktschuss, im Nether × 8 ≈ 150.000 Oberwelt-Äquivalent. | Folia `0001-Region-Threading-Base.patch` (paper-patches Zeile 107–150, minecraft-patches Zeile 16717 ff., 11340 ff.) |
| Folia: Besitzer in anderer Region | `Projectile.getOwner()` gibt `null` zurück, wenn der Besitzer auf einem anderen Thread ist. Dann setzt die Perle kein Vanilla-Ticket mehr (das Moonrise-Ticket bleibt). Der Teleport läuft über `getOwnerRaw()` und `teleportAsync`, funktioniert also auch regionsübergreifend. | Folia-Patch `Projectile`, `ThrownEnderpearl` |
| Weltgrenze | 26.3: Projektile prallen an der Weltgrenze ab ([perlenkanone.md](perlenkanone.md) 3.4). Für 100.000 Blöcke unerheblich. | – |

---

## 4. Kosten

### 4.1 Explosionen und Material pro Schuss

Annahmen: bester Winkel, erster Tick in Wasser (Perlenhalter mit Blasensäule wie Bean Blaster und Rays Works), alle Explosionen voll wirksam. Δv: TNT 0,75–0,90, Windkugel 0,53–0,62 (Bean-Blaster-Wert), Breeze-Kugel ≈ 0,66 (Rückrechnung 2.3). *Alles hergeleitet.* Waagerechte Bauten brauchen etwa 1,5× so viel.

**Direkt in der Oberwelt:**

| Weite | TNT (Schwarzpulver) | Windkugeln (Breeze-Ruten) | Breeze-Kugeln (Ladezeit mit 1 Breeze) |
|---|---|---|---|
| 10.000 | 141–169 TNT (705–845 SP) – **auf Paper/Folia unmöglich** (> 100 TNT/Tick) | 205–240 (51–60 Ruten) | ≈ 190 (≈ 4,6 min) |
| 25.000 | 349–419 TNT (1.745–2.095 SP) – unmöglich auf Paper | 506–592 (127–148) | ≈ 475 (≈ 11,5 min) – *auf Folia über der Speed-Grenze* |
| 50.000 | 696–835 TNT (3.480–4.175 SP) – unmöglich auf Paper | 1.010–1.181 (253–295) | ≈ 950 (≈ 23 min) – *Folia: nein* |
| 100.000 | 1.390–1.668 TNT (6.950–8.340 SP) – unmöglich auf Paper | 2.018–2.360 (505–590) | ≈ 1.900 (≈ 46 min) – *Folia: nein* |

**Über den Nether (× 8, Start über dem Netherdach):**

| Oberwelt-Äquivalent | TNT (Schwarzpulver) | Windkugeln (Ruten) | Breeze-Kugeln (Ladezeit mit 1 Breeze) |
|---|---|---|---|
| 10.000 | 20–24 (100–120 SP) | 28–33 (7–9) | ≈ 27 (≈ 40 s) |
| 25.000 | 46–55 (230–275 SP) | 66–77 (17–20) | ≈ 62 (≈ 1,5 min) |
| 50.000 | 89–107 (445–535 SP) – knapp am 100-TNT-Limit | 129–151 (33–38) | ≈ 121 (≈ 3 min) |
| 100.000 | 176–211 (880–1.055 SP) – **über dem 100-TNT-Limit** | 255–298 (64–75) | ≈ 240 (≈ 6 min) |
| 160.000 (Rays Works, waagerecht, 576 Kugeln) | – | – | 576 (≈ 14 min) |

Dazu einmalig pro Ziel: ein Netherportal am Landepunkt (10–14 Obsidian, Feuerzeug), wenn dort keins steht.

### 4.2 Material ohne Duping farmen

| Material | Quelle und Rate | Beleg |
|---|---|---|
| **Breeze-Ruten** | Nur von Breezes, nur bei Tod durch Spieler oder Wolf: 1–2 pro Breeze, mit Plünderung III 4–8. Breezes gibt es nur aus Trial Spawnern. **Normaler Spawner:** 2 Breezes (+1 pro weiterem Spieler), danach 30 min Abklingzeit. **Ominöser Spawner** (braucht Trial Omen): 4 Breezes (+1 pro Spieler). *Hergeleitet:* ein Spieler mit Plünderung III ≈ **24 Ruten/h** pro normalem Spawner, ≈ 48/h ominös; ohne Plünderung 6 bzw. 12/h. Also **100.000 Blöcke direkt (505–590 Ruten) ≈ 11–25 h Farmen**, über den Nether (64–75 Ruten) ≈ 1,5–3 h. | `trial_spawner/…/breeze/normal.json` (total_mobs 2), `ominous.json` (4), `loot_table/entities/breeze.json`, [Wiki: Trial Spawner](https://minecraft.wiki/w/Trial_Spawner) (36.000 Ticks), [Wiki: Breeze](https://minecraft.wiki/w/Breeze) |
| Windkugeln direkt | Truhen/Tresore der Trial Chamber: `reward_common` 1–3 oder 4–12 (Gewicht 4/25), `reward_ominous_common` 8–12 (4/15). Jeden Tresor kann jeder Spieler nur einmal öffnen. Ominöse Item-Spawner werfen 1–3 Windkugeln (Gewicht 1/5). Nicht nachhaltig. | Loot-Tabellen im Jar, [Wiki: Vault](https://minecraft.wiki/w/Vault) |
| **Breeze-Kugeln** | Kostenlos, ≈ 2.500/h pro gefangener Breeze (hergeleitet aus `breeze.Shoot`); mehr Breezes = schneller (CubeControl). | 2.3 |
| Schwarzpulver | Creeper: 0–2 (+0–1 pro Plünderungsstufe), Ghast ebenso + 0–1 Träne. Creeper-Farm laut Wiki: „around 700 gunpowder per hour“ (Chapman) bis „over 10 stacks“ in 5 min (SB737/Namiature-Variante, also ≈ 7.000/h). | `loot_table/entities/creeper.json`, `ghast.json`, [Wiki](https://minecraft.wiki/w/Tutorial:Creeper_farming) |
| Sand | 4 pro TNT. Abbau in Wüsten. Sand-Duper sind Dupes und daher ausgeschlossen. | `recipe/tnt.json` |
| Ghast-Tränen (Kristalle) | 0–1 pro Ghast. Für Kanonen ohne Nutzen (2.5). | `ghast.json` |

---

## 5. 6b6t

- **Erlaubt:** Keine Regel gegen Kanonen, Breezes, Golems oder Redstone ([perlenkanone.md](perlenkanone.md) 5). Netherdach-Zugang ist durch Fox' **Bedrock breaking** eine Server-Funktion ([Issue #12](https://github.com/6b6t/feedback/issues/12)).
- **Was die großen Kanonen trifft:**
  - **Perlen-Verlust beim Worker-Wechsel ([#7](https://github.com/6b6t/feedback/issues/7), offen):** 6b6t teilt die Welt auf mehrere „Worker“ auf (`WorkerChunkOwnership`, Leases, [#28](https://github.com/6b6t/feedback/issues/28)). Ist der Besitzer beim Aufprall „between workers“, verwirft Fox die Perle. Ob eine **Perle selbst** beim Flug über eine Worker-Grenze übertragen wird, steht nirgends. Bei 12.500 Nether-Blöcken ist ein Grenzübertritt wahrscheinlich. **Unbekannt, größtes Risiko.**
  - **Stasis-Bug ([#7](https://github.com/6b6t/feedback/issues/7)):** Die Perle verschwindet ≈ 2 s nach jedem Teleport des Besitzers. Beim Laden einer Breeze-Kanone (Minuten) darf man sich nicht teleportieren.
  - **Folia-Region-Grenze (Abschnitt 3):** Direktschüsse über ≈ 240 b/t frieren *vermutlich* ein. Oberwelt-Direktschüsse über ≈ 19.000 Blöcke sind auf 6b6t also nicht planbar; der Nether-Weg bleibt bis ≈ 150.000 Oberwelt-Äquivalent darunter. Fox kann `grid-exponent` geändert haben (unbekannt).
  - **Ankunft in ladendem Gebiet ([#28](https://github.com/6b6t/feedback/issues/28), offen):** Kick auf den Backup-Server, laut [#1](https://github.com/6b6t/feedback/issues/1) teils 5–6 h kein Rejoin. Ein Perlenschuss in frisch generiertes Gebiet ist genau dieser Fall.
  - **Chunk-Generierung ([#1](https://github.com/6b6t/feedback/issues/1), geschlossen ohne genannten Fix):** Im Nether stoppte die Generierung beim Elytra-Flug nach ≈ 1.000 Blöcken. Ob das eine Perle trifft, ist unbekannt.
  - **TNT:** `max-tnt-per-tick` (Standard 100) → TNT-Kanonen nur bis ≈ 7.000–8.800 Blöcke direkt bzw. ≈ 50.000 über den Nether. Wert auf 6b6t unbekannt.
  - **Version:** 26.2. Alle hier genutzten Klassen sind laut [perlenkanone.md](perlenkanone.md) und [bean-blaster.md](bean-blaster.md) zwischen 26.2 und 26.3 gleich; Breeze-Klassen habe ich nur in 26.2 gelesen.
- **Empfohlener Test:** zuerst kurze Nether-Schüsse (≤ 1.000 Nether-Blöcke) in schon generiertem Gebiet, Besitzer bleibt neben der Kanone. Dann schrittweise weiter.

---

## 6. Fazit

**Ja, es gibt eine dupe-freie Bauweise für 100.000+ Blöcke, die bezahlbar ist:** eine **Windkugel-Kanone, die die Perle durch ein Netherportal über das Netherdach schießt** (Faktor 8). Am billigsten ist die Variante mit **gefangener Breeze**, die ihre eigenen Windkugeln liefert (Rays Works, CubeControl): **0 Material pro Schuss**, ≈ 240 Kugeln bzw. ≈ 6 Minuten Laden für 100.000 Blöcke. Mit Spender-Windkugeln kostet derselbe Schuss ≈ 64–75 Breeze-Ruten.

**Ohne Nether** ist 100.000 in der Oberwelt zwar physikalisch machbar (≈ 1.900 Breeze-Kugeln oder ≈ 2.000–2.400 Windkugeln = 505–590 Ruten), aber auf Folia/6b6t *vermutlich* nicht, weil die Perle oberhalb von ≈ 240 b/t nicht mehr tickt. TNT ohne Duper scheitert auf jedem Paper-Server schon ab ≈ 7.000–8.800 Blöcken am `max-tnt-per-tick`-Limit. End-Kristalle, Anker, Ghast-Feuerbälle, Kolben und Fahrzeuge skalieren überhaupt nicht.

**Rangliste (für 6b6t):**
1. Breeze-Kanone + Nether × 8: 0 pro Schuss, einmalig Breeze fangen; bis ≈ 150.000 unterhalb der Folia-Grenze. Risiken #7/#28 und Worker-Grenzen.
2. Bean Blaster (Spender-Windkugeln) + Nether × 8: 7–9 Ruten pro 10.000, 64–75 Ruten pro 100.000.
3. Breeze-Kanone/Bean Blaster direkt in der Oberwelt: bis ≈ 19.000 Blöcke (Folia-Grenze), 51–60 Ruten pro 10.000.
4. TNT aus Spendern + Nether: ≈ 100–120 Schwarzpulver pro 10.000, ≈ 445–535 pro 50.000; über ≈ 50.000 am TNT-Limit.
5. TNT direkt: höchstens ≈ 7.000–8.800 Blöcke auf Paper/Folia.

---

## 7. Was nicht gefunden bzw. nicht geprüft wurde

- **Nichts im Spiel getestet.** Alle Weiten, Kugelzahlen und Zeiten sind simuliert, hergeleitet oder aus Erfinder-Aussagen zurückgerechnet.
- **Rays Works:** Nur das automatische Transkript, keine Blockliste. Welt-Download und `.nbt` (Mediafire) nicht geladen. Die Δv von 0,66 pro Breeze-Kugel ist aus seinen eigenen Weitenangaben zurückgerechnet, also nicht unabhängig.
- **CubeControl:** Nur die Beschreibung, kein Transkript, kein Bauplan.
- **Folia-Speed-Grenze** nur aus dem Code abgeleitet; nicht getestet, ob die eingefrorene Perle später doch weiterfliegt (z. B. wenn Regionen zusammenwachsen).
- **Fox (6b6t):** Quellcode nicht öffentlich. Unbekannt: Worker-Übergang für fliegende Perlen, `max-tnt-per-tick`, `grid-exponent`, `nether-ceiling-void-damage-height`, `EntityLimits` für `wind_charge`/`breeze_wind_charge`, `bedrock-breaking-interval`.
- **Durchgang einer Perle durchs Netherportal unter Folia** nicht im Code verfolgt (nur Vanilla `NetherPortalBlock`).
- Breeze-Zielwahl (Eisengolem) und Despawn-Verhalten gefangener Breezes nur grob bzw. gar nicht im Code geprüft. Trial-Omen-Dauer nicht geprüft.
- Windstoß-Verzauberung (Streitkolben) nicht im Code geprüft.
- Keine dokumentierte Staffel-Route und keine Mehrstufen-Kanone gefunden.
- Discord (6b6t, TMC) nicht gelesen; „836 FTL“ ist laut Wiki nur im TMC-Discord.

---

## Kernzahlen

| Größe | Wert | Quelle |
|---|---|---|
| Startgeschwindigkeit für 100.000 Blöcke | 1.011 b/t (ohne Wasser) / 1.251 b/t (1. Tick Wasser) | Simulation, 1.1 |
| Stoß pro Explosion | ≤ ≈ 1 b/t, nur Windkugel ×1,22 | `ServerExplosion`, 1.2 |
| Nether-Faktor | 8; Perle behält Geschwindigkeit (`Relative.DELTA`) | `NetherPortalBlock`, 2.4 |
| 100.000 über den Nether | 158 b/t ≈ 240 Breeze-Kugeln / 255–298 Windkugeln (64–75 Ruten) / 176–211 TNT | 4.1 |
| Breeze-Schuss-Takt | 15 + 4 + 10 Ticks ≈ 1,45 s pro Kugel | `breeze.Shoot` |
| TNT-Limit Paper/Folia | 100 TNT/Tick (pro Region) → ≤ ≈ 7.000–8.800 Blöcke direkt | `SpigotWorldConfig`, `PrimedTnt`-Patch |
| Folia-Speed-Grenze (hergeleitet) | ≈ 240 b/t pro Achse sicher | Folia `ThrowableProjectile`-Patch |
| End-Kristalle | löschen sich gegenseitig ohne Explosion → nicht stapelbar | `EndCrystal.hurtServer` |
| Breeze-Ruten | ≈ 24/h pro Trial Spawner mit Plünderung III (solo) | Datenpaket, Wiki |
