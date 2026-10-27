# Bean Blaster: Perlenkanone mit Windkugeln (MC 1.21.x / 26.2 / 26.3) – Recherche

Stand: 2026-10-27. Ziel: verstehen, wie der „Bean Blaster“ funktioniert. Das ist eine Enderperlen-Kanone mit Windkugeln aus Spendern und einem Magmablock. Außerdem: wie man ihn baut, was er kostet, was auf 6b6t dagegen spricht und was der Cheecky Client (MC 26.3) dabei automatisieren kann.
**Nicht im Umfang:** Anti-Cheat-Umgehung, Exploits und Dupes. Die Grundlagen zu Explosionen, Perlenflug und 6b6t stehen schon in [perlenkanone.md](perlenkanone.md) und [6b6t-regeln-limits.md](6b6t-regeln-limits.md). Hier steht nur, was beim Bean Blaster neu oder anders ist.

Alle Quellen wurden am **2026-10-27** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Video „This Redstone machine is /TP in survival - 360 precise ender pearl cannon“ von **IddieBean** ([09y7VMpN6-g](https://www.youtube.com/watch?v=09y7VMpN6-g), veröffentlicht 2026-03-26) | Video des Erfinders | Primärquelle. **Automatisches Transkript** gelesen, Bild nicht gesehen |
| Video „How to get the bean blaster into your world“ von IddieBean ([80bDoqLqX9Y](https://www.youtube.com/watch?v=80bDoqLqX9Y)) | Aufbau-Anleitung des Erfinders | Primärquelle, ebenfalls nur das Transkript |
| [Planet Minecraft: Bean Blaster](https://www.planetminecraft.com/project/bean-blaster/) (Welt-Download, `.litematic`, Python-Code), [beanblaster.tems.pl](https://beanblaster.tems.pl/) (Rechner-Website von „Tems“, in der Videobeschreibung verlinkt) | Downloads des Erfinders | **Nicht gelesen**: Beide Seiten blockieren automatische Abrufe (403 bzw. Cloudflare-Prüfung) |
| Mod „Bean Blaster Calculator“: [Modrinth](https://modrinth.com/mod/bean-blaster-calculator) (Version 1.0.15, nur 1.21.11, Fabric, MIT), Quellcode [IamSboby/bean-blaster-calculator-mcmod](https://github.com/IamSboby/bean-blaster-calculator-mcmod) (Commit `b3d74b7`, 1.0.16), Release-Jar 1.0.15 von Modrinth per `javap`, [mado-hosting.com](https://www.mado-hosting.com/minecraft-mods/bean-blaster-calculator) (Spiegel der Modrinth-Seite) | Mod-Quellcode | Primärquelle für die Rechenformel. Laut Mod ist der Code ein „Pure Java port of the original Python Bean Blaster formulas“ |
| Spiel-Quellcode 26.3 (`minecraft-merged-7e9a32a5b8-26.3-sources.jar`), Bytecode 26.2 (`…-201bed67e3-26.2.jar`) aus `.gradle/loom-cache`, Bytecode 1.21.11 (offizielle Server-Jar + Mojang-Mappings von `piston-meta.mojang.com`) | Spielcode | Primärquelle |
| [minecraft.wiki: Wind Charge](https://minecraft.wiki/w/Wind_Charge), [Breeze Rod](https://minecraft.wiki/w/Breeze_Rod) | Wiki | Primärquelle (Community) |
| [github.com/6b6t/feedback](https://github.com/6b6t/feedback) (Issues #7, #28), [Paper-Weltkonfiguration](https://docs.papermc.io/paper/reference/world-configuration/) | Betreiber / Server-Doku | offiziell (Issues = interne Arbeitsnotizen) |

---

## 1. Was der Bean Blaster ist

- Erfinder ist **IddieBean** (YouTube). Er nennt die Maschine eine „redstone machine capable of firing ender pearls thousands of blocks in just about a minute, but with pinpoint accuracy“. Als Inspiration nennt er **EthosLab** und **Cubicmetre** ([Video](https://www.youtube.com/watch?v=09y7VMpN6-g), Beschreibung).
- Die Vorgeschichte laut Video: Früher hat er Kanonen mit „wind charged“-Mobs gebaut (Silberfische). Die waren zu ungenau. Dann sah er eine Maschine von EthosLab (Hermitcraft, ein „Player Launcher“) mit einem Baustein, der „stores a wind charge from a dispenser and freezes it in the air“. Mit vielen solchen gespeicherten Windkugeln kann man Entities sehr stark beschleunigen ([Video](https://www.youtube.com/watch?v=09y7VMpN6-g), ab 3:07). *Das EthosLab-Video selbst habe ich nicht gefunden und nicht geprüft.*
- Den Namen „Bean Blaster“ hat er selbst vergeben. Laut Video läuft die Maschine nur in Java: „This is Java only, and this works in every Java version I've played so far.“ Welche Versionen das sind, sagt er nicht. Der Rechner-Mod ist nur für **1.21.11** gebaut (Abschnitt 4).

## 2. Bauweise

### 2.1 Laut Erfinder (Transkript)

| Baugruppe | Was IddieBean sagt | Stelle |
|---|---|---|
| Vier Windkugel-Speicher | „Four wind charge stores lay across each other“. Jeder Speicher hält die Windkugeln aus *einem* Spender in der Luft fest | Video 1, 4:30 |
| Vier Spender (Nr. 1–4) | Ein Spender pro Seite der Kammer. Man legt die vom Rechner genannte Anzahl Windkugeln hinein. Ein „simple togglable redstone clock“ schaltet sie schnell an und aus. Probleme gab es mit Quasi-Konnektivität und Leitungen, die „one block off to the left“ liegen mussten | Video 1, 3:55 und 4:21; Video 2, 7:44 |
| Vier Schleimblöcke an Kolben | „Four slime blocks push the wind charges down [with] precise timing so that the wind charges explode on four sides of an ender pearl stasis chamber“ | Video 1, 4:33 |
| Perlen-Kammer | Eine **Blasensäule nach oben (Seelensand) direkt neben einer Blasensäule nach unten (Magma)**: „the pulling upwards of the soul sand will be perfectly canceled out by the pulling downwards of the magma, meaning the ender pearl just stays in place“ | Video 1, 5:37 |
| Ausrichtung der Perle | **Honigblöcke** schieben die Perle „into the exact corner of a block“, danach schiebt ein Schleimblock sie in die endgültige Lage | Video 1, 5:27 und 5:49 |
| Einwurf | Oben ein Wasserloch mit Pfeil nach unten. Die Perle fällt in eine zweite Kammer. Immer nur **eine** Perle einwerfen, sonst verschiebt sich etwas | Video 2, 4:39–5:30 |
| Ausgleich für Fehlschüsse | Der Speicher hält eine Windkugel nur in ~95 % der Fälle. Ein Hebel erkennt jedes Platzen einer Windkugel („wind charges … flip levers“, siehe 3.2) und lässt aus einem **Trichter** neben dem Spender eine weitere nachlegen. Empfehlung: „about a stack or two“ Windkugeln pro Trichter | Video 1, 6:04; Video 2, 6:17–7:15 |
| Bedienplatz | Granitlinie zum Stehen, Leiter, Glas zum Zuschauen. Man soll nicht vom Granit treten, sonst treffen einen die Windkugeln oder die Perle. Einige Hebel gehören zur Maschine und dürfen nicht umgelegt werden | Video 2, 5:36–6:30, 10:09–10:30 |
| Höhe | Der **Magmablock muss genau auf Y 250 stehen**, weil die Maschine für diese Höhe kalibriert ist. In der Download-Welt liegt der Boden auf Y 82, also etwa 170 Blöcke tiefer. Die X/Z-Position ist egal | Video 2, 0:20–0:57 |

**Genaue Blockliste:** Eine Liste mit Stückzahlen gibt es nur als Welt-Download oder `.litematic` auf [Planet Minecraft](https://www.planetminecraft.com/project/bean-blaster/), und den konnte ich nicht abrufen (siehe Abschnitt 8). Sicher belegt sind nur diese Bauteile: 1 Magmablock, mindestens 1 Seelensand, Wasser, 4 Spender, 4 Trichter, Kolben (Klebekolben unbekannt) mit Schleimblöcken, Honigblöcke, Hebel, Redstone mit Takt, Granit, Steinziegel, Glas und Leiter. Die Kolben im Windkugel-Speicher kommen in 2.2 dazu.

### 2.2 Wie die Teile funktionieren (Spielcode 26.3, Abgleich 26.2 und 1.21.11)

**Windkugel aus dem Spender** (`WindChargeItem`, `ProjectileDispenseBehavior`, `Projectile.getMovementToShoot`):
- Der Spender setzt die Windkugel auf die Mitte des Blocks vor sich (`getDispensePosition(source, 1.0, ZERO)`). Er schießt sie mit Stärke `1.0` und Ungenauigkeit `6.6666665` ab. Je Achse kommt ein Zufallswert `triangle(0, 0.0172275·6.667) = ±0.11485` dazu. Die Windkugel fliegt also mit etwa **1 Block/Tick** und streut um bis zu ±0,115 je Achse.
- Pro Auslösung gibt der Spender **genau eine** Windkugel aus. Die Auslösung kommt 4 Ticks nach der steigenden Flanke (`DispenserBlock.neighborChanged`: `scheduleTick(pos, 4)`). Solange ein Tick für dieselbe Position wartet, wird kein zweiter geplant (`LevelChunkTicks.schedule`, Vergleich nur über Position und Typ). **Hergeleitet:** höchstens etwa **1 Windkugel alle 4 Ticks pro Spender (5/s)**. Für 124 Windkugeln dauert das Laden also ≈ 25 s, bei parallelen Spendern nicht länger. Das passt zu „in just about a minute“. Auch die Quasi-Konnektivität steht im Code: `hasNeighborSignal(pos.above())`.

**Windkugel im Flug** (`AbstractHurtingProjectile`, `AbstractWindCharge`):
- `accelerationPower = 0` und `getInertia() = 1.0`, auch in Wasser (`getLiquidInertia = getInertia`). **Die Windkugel wird weder schneller noch gebremst und fällt nicht.** Ist ihre Geschwindigkeit einmal null, bleibt sie für immer an ihrer Stelle in der Luft. Bedingung ist nur, dass der Chunk geladen ist und es keinen entfernten Besitzer gibt (`tick`: `owner == null || !owner.isRemoved()`). Aus Spendern kommen Windkugeln **ohne Besitzer**.
- Wie man sie anhält, steht im Wiki: „Wind charges lose all velocity when they collide with a moving piston head“ ([Wiki](https://minecraft.wiki/w/Wind_Charge)). *Den genauen Codepfad habe ich nicht nachverfolgt.* Das ist der „Speicher“ von EthosLab.
- `AbstractWindCharge.push(…)` ist **leer**. Explosionen schieben Windkugeln deshalb nicht (`pushFromExplosion` → `push`). `canCollideWith` und `canHitEntity` geben gegen andere Windkugeln `false` zurück. **Folge:** Hunderte Windkugeln können an derselben Stelle liegen und im selben Tick explodieren, ohne sich gegenseitig wegzuschieben. Genau daran scheitert die TNT-Kanone ([perlenkanone.md](perlenkanone.md) 1.5, „TNT schiebt TNT“).
- **Schleimblock-Stoß** (`PistonMovingBlockEntity.moveCollidedEntities`): Ein bewegter Schleimblock setzt die Geschwindigkeit jeder berührten Entity auf der Bewegungsachse auf genau **±1** (`dx = movement.getStepX()` usw.). In 26.2 gilt das für alle Entities außer `ServerPlayer`, in 26.3 für alle mit `canSimulateMovement()`. Für Windkugeln auf dem Server ist das Ergebnis gleich. So schickt der Schleimblock alle gespeicherten Windkugeln gleichzeitig mit 1 Block/Tick nach unten.
- **Aufprall:** Die Windkugel explodiert am Trefferpunkt plus 0,25 Blöcke entlang der Flächennormalen (`onHitBlock`). Die Explosion hat Stärke `1.2`, Reichweite für Entities also 2,4 Blöcke, und Rückstoß-Multiplikator **`1.22`** (`WindCharge`, `SimpleExplosionDamageCalculator(…, Optional.of(1.22F), …)`). Blöcke bleiben heil, ausgelöst werden aber Hebel, Knöpfe, Türen usw. (`ExplosionInteraction.TRIGGER`, [Wiki](https://minecraft.wiki/w/Wind_Charge)). Darauf beruht der Hebel-Detektor.
- **Treffen Windkugeln die Perle?** Nein. `Projectile.canHitEntity` verlangt `isPickable()`, und das gilt bei Projektilen nur für den Tag `redirectable_projectile` (`fireball`, `wind_charge`, `breeze_wind_charge`). Die Perle ist nicht darin. **Umgekehrt kann die Perle aber eine Windkugel treffen**, denn Windkugeln sind im Tag. Die Flugbahn der Perle darf also nicht durch einen Speicher führen. *Hergeleitet aus dem Code, nicht getestet.*

**Warum der Magmablock** (`Entity.handleOnInsideBubbleColumn`, `BubbleColumnBlock.entityInside`, `ThrowableProjectile.tick`):
- In einer Blasensäule nach oben (Seelensand) gilt `vy = min(0.7, vy + 0.06)`, nach unten (Magma) `vy = max(−0.3, vy − 0.03)`. `entityInside` wird für **jeden** Säulenblock aufgerufen, den die Hitbox berührt. Eine Perle (0,25 × 0,25) auf der Grenze zwischen beiden Säulen bekommt also beide Effekte.
- Pro Tick rechnet die Perle so: Schwerkraft −0,03 → Wasser-Trägheit × 0,8 → Bewegung → Säuleneffekte +0,06 − 0,03. **Hergeleitet:** Der Fixpunkt ist `vy = 0.03` vor dem Tick. Die Bewegung pro Tick beträgt dann `0.8 · (0.03 − 0.03) = 0`. Die Perle steht also exakt still. Das bestätigt die Aussage „perfectly canceled out“.
- Daraus folgen Bedingungen (*hergeleitet, nicht getestet*):
  - Über der Perle muss Wasser oder ein Block sein. Sonst greift `onAboveBubbleColumn` (+0,1 bzw. −0,03), und die Werte heben sich nicht mehr auf.
  - Berührt die Perle in einer Säule zwei Blöcke übereinander, zählt der Effekt doppelt, und sie driftet.
- Eine Seelensand-Säule allein hält die Perle nur oben an der Oberfläche ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl), siehe [perlenkanone.md](perlenkanone.md) 2.1). Dort wippt sie, und dieses Wippen nennt IddieBean als Hauptquelle für Streuung („The slight bobbing of the ender pearl creates massive random variation“). Der Magmablock hält die Perle *unter* Wasser still und dient zugleich dem Rechner als Bezugspunkt (Abschnitt 4).

### 2.3 Ablauf eines Schusses (aus 2.1 und 2.2 zusammengesetzt)

1. Mit dem Rechner die Anzahl Windkugeln für 2 der 4 Spender bestimmen (Abschnitt 4).
2. Die Windkugeln in die Spender legen, die Trichter mit Reserve füllen.
3. Eine Perle oben einwerfen. Honig und Schleim richten sie aus, danach steht sie zwischen Seelensand- und Magmasäule still.
4. Den Takt starten. Die Spender schießen ihre Windkugeln nacheinander in ihre Speicher, wo sie in der Luft stehen bleiben. Geht eine verloren, legt der Hebel- und Trichter-Mechanismus nach.
5. Die Schleimblöcke schieben alle Windkugeln eines Speichers mit 1 Block/Tick nach unten, an die Seiten der Kammer. Dort explodieren sie, laut Video „with precise timing“, *also vermutlich im selben Tick* (siehe 3.1).
6. Die Perle fliegt von Y ≈ 250 schräg nach unten und landet etwa 170 Blöcke tiefer. Der Spieler wird dorthin teleportiert.

Wie der Spieler den Takt startet und ob die Schleimblöcke von selbst auslösen, wenn der Spender leer ist, geht aus dem Transkript **nicht** hervor.

---

## 3. Physik

### 3.1 Stoß pro Windkugel

Nach `ServerExplosion.hurtEntities` (Formel in [perlenkanone.md](perlenkanone.md) 1.1; in 26.3 kommt nur ein Team-Check für den *Schaden* dazu, der Rückstoß bleibt gleich):

```
Δv = (1 − d/2.4) · exposure · 1.22      [Blöcke/Tick]
d = Abstand Explosionszentrum → Perlen-Füße, Richtung → Perlen-Auge (Füße + 0,2125)
```

Die Augenhöhe 0,2125 = 0,85 × 0,25 ist jetzt im Code bestätigt (`EntityDimensions`: `height * 0.85F`, kein `eyeHeight` in `EntityTypes.ENDER_PEARL`). Damit ist eine Lücke aus perlenkanone.md geschlossen.

| d | 0,5 | 0,75 | 1,0 | 1,25 | 1,5 | 2,0 |
|---|---|---|---|---|---|---|
| Δv bei voller Exposure | 0,97 | 0,84 | 0,71 | 0,59 | 0,46 | 0,20 |

- **Mehrere Windkugeln addieren sich**, aber nur, wenn sie im *selben Tick* explodieren, solange die Perle noch an ihrem Platz liegt (`Entity.push`: `deltaMovement += v`). Explodieren sie über mehrere Ticks verteilt, hat sich die Perle zwischendurch schon bewegt (Wasser bremst nur um × 0,8), und spätere Explosionen treffen sie schwächer oder gar nicht. *Hergeleitet.* Dass die Weite im Rechner exakt **linear** mit der Anzahl wächst (Abschnitt 4), spricht dafür, dass alle Windkugeln im selben Tick explodieren.
- Weil Windkugeln einander nicht schieben (2.2), explodieren alle an derselben Stelle. Der Stoß ist also N · Δv, mit derselben Richtung bei jeder Kugel.

### 3.2 Flugweite (hergeleitet, eigenes Skript)

Die horizontale und die vertikale Bewegung der Perle sind unabhängig: Der Luftwiderstand multipliziert jede Achse einzeln mit 0,99, die Schwerkraft wirkt nur auf y. Für einen Stoß, der **rein horizontal** ist, gilt deshalb:

```
Weite = v_h · S(t_Landung)       S(t) = Σ Trägheit_i  (erster Tick in Wasser: 0,8, danach 0,99)
t_Landung hängt nur von der Fallhöhe ab
```

Die Weite wächst also **linear** mit der Zahl der Windkugeln. Genau so rechnet IddieBean: „creating a linear graph on Desmos“ (Video 1, 7:24). Simuliert mit der Tick-Reihenfolge aus `ThrowableProjectile.tick` (Schwerkraft → Trägheit → Bewegung):

| Fallhöhe | Flugzeit | S (1 Tick Wasser) | S (ohne Wasser) |
|---|---|---|---|
| 100 | 95 Ticks | 49 | 61 |
| 168 (Y 250 → Y 82, Kalibrierwelt) | 129 Ticks (6,5 s) | **58,1** | 71,9 |
| 200 | 143 Ticks | 61 | 75 |

- Aus den Werten des Erfinders (31–36 Blöcke pro Windkugel, Abschnitt 4) folgt mit S = 58,1: **Δv ≈ 0,53–0,62 Blöcke/Tick pro Windkugel**. Nach der Tabelle in 3.1 entspricht das einem Abstand von etwa 1,2–1,35 Blöcken bei voller Exposure. Das ist für eine Explosion „an der Seite der Kammer“ plausibel. *Rückrechnung, nicht gemessen.*
- **Einfluss der Landehöhe:** Der Rechner kennt nur X/Z. Liegt das Ziel tiefer oder höher als Y 82, ändert sich die Flugzeit:

  | Boden am Ziel | Y 40 | Y 63 | Y 82 | Y 100 | Y 120 |
  |---|---|---|---|---|---|
  | Faktor auf die Weite | 1,065 | 1,033 | 1,000 | 0,968 | 0,925 |

  Bei 10.000 Blöcken sind das schon ±300–750 Blöcke. Bäume und Berge auf der Flugbahn beenden den Flug früher. *Hergeleitet.* IddieBean misst etwa 80 Blöcke Abweichung bei ~1.280 Blöcken und schätzt „a range of round about 100, 150 blocks“ (Video 2, 10:34–10:53).
- **Obergrenze pro Spender:** 9 Slots × 64 = **576 Windkugeln** (so steht es auch im Mod). Mit 31–36 Blöcken pro Windkugel sind das ≈ **17.900–20.700 Blöcke pro Achse**. Diagonal mit zwei vollen Spendern kommt man auf ≈ 25.000–29.000 Blöcke. *Hergeleitet, und bei so großer Weite ungetestet* (Chunk-Laden, siehe 5).

| Windkugeln (ein Spender, entlang der Achse) | Weite (31–36 Bl./Kugel) |
|---|---|
| 1 | 31–36 |
| 10 | 310–360 |
| 32 | ≈ 1.000–1.150 |
| 64 (1 Stack) | ≈ 2.000–2.300 |
| 150 | ≈ 4.650–5.400 |
| 300 | ≈ 9.300–10.800 |
| 576 (voll) | ≈ 17.900–20.700 |

---

## 4. Der Rechner (Mod-Quellcode)

`BeanBlasterCalculator.calculate` (Commit `b3d74b7`, gleiche Konstanten im Release-Jar 1.0.15) berechnet keine Physik. Er arbeitet mit einem **empirischen linearen Modell aus vier Richtungsvektoren**, einem pro Spender:

| Spender | Richtung (Steigung) | Blöcke pro Windkugel |
|---|---|---|
| 1 | −X, Drift +Z (Steigung 1/15,0) | 31 (Sektor „NW“) bzw. 36 (Sektor „SW“) |
| 2 | +X, Drift +Z (1/13,5) | 31 („NE“) bzw. 36 („SE“) |
| 3 | +Z, Drift +X (1/13,7) | 36 |
| 4 | −Z, Drift +X (1/15,2) | 31 |

Der Algorithmus:
1. Ziel relativ zum Magmablock nehmen (`target − machine`, nur X/Z).
2. Über vier Geradenvergleiche den Sektor wählen. Das bestimmt, welche zwei benachbarten Spender schießen.
3. Den Schnittpunkt der Geraden „Spender A durch den Ursprung“ und „Spender B durch das Ziel“ berechnen (`crossoverX = (f·tz − tx)/(f·o − 1)`).
4. Die Strecken durch die Blöcke pro Kugel teilen und **abschneiden** (`(int)`, wie Pythons `int()`).

Das stimmt mit dem Video überein: Dispenser 1 = „31 blocks in the negative X axis and 2.05 blocks in the [Z] axis“ (31/15 = 2,07). Beispiel ⟨1500, 600⟩ → „13 and 47“. Beispiel ⟨−999, 800⟩ → „dispenser one, 33 and dispenser three, 20“. Mein Port in Python liefert dieselben Werte.

Auffälligkeiten (eigene Prüfung):
- **Spender 1 und 2 haben je nach Sektor 31 oder 36 Blöcke pro Kugel.** Physikalisch ist es derselbe Spender. Entweder ist das ein Fehler im Original, bei dem die Teiler in den Süd-Sektoren vertauscht sind, oder eine Messung, die nicht erklärt wird. *Ohne den Python-Originalcode nicht klärbar.*
- Die Sektor-Namen passen nicht zu Minecraft: „NE“ ist +X/+Z, also eigentlich Südost.
- Durch das Abschneiden fällt jeder Schuss im Mittel etwa eine halbe Windkugel (≈ 15–18 Blöcke) pro Spender zu kurz aus.
- Liegt das Ziel genau auf einer Achse, nutzt der Rechner trotzdem zwei Spender, z. B. ⟨1000, 0⟩ → Spender 2: 27, Spender 4: 2. Der Grund ist der schräge Verlauf der Achsen.
- **Befehl:** Laut README und Modrinth heißt er `/bb` bzw. `/beanblaster`. Der Quellcode und das Release-Jar 1.0.15 registrieren aber nur **`/blaster`** (`literal("blaster")`, Bytecode-Konstante `String blaster`). Die Doku ist also falsch.
- Die Kalibrierung gilt nur für **diesen einen Bau auf Y 250**. Ein eigener Nachbau mit anderer Geometrie braucht eigene Konstanten.

---

## 5. Versionen: 1.21.x vs. 26.2 vs. 26.3

| Thema | Ergebnis | Grundlage |
|---|---|---|
| Windkugel-Konstanten (Stärke 1.2, Rückstoß 1.22, Trägheit 1.0, `push` leer, kein Treffer gegen andere Windkugeln, Spender-Streuung 0.11485, Stärke 1.0) | **gleich in 1.21.11, 26.2 und 26.3** | Bytecode 1.21.11 (`dev`, `det`, `dnm` laut Mojang-Mappings), Diff 26.2↔26.3 |
| `AbstractHurtingProjectile`, `WindChargeItem`, `DispenserBlock`, `BubbleColumnBlock`, `HoneyBlock` | 26.2 → 26.3: keine Logikänderung (nur Codec-Felder und Konstanten-Pool) | normalisierter Bytecode-Diff |
| `WindCharge.deflect` | 26.3: zusätzlicher Parameter `Vec3 power`. Für den Bean Blaster unerheblich | Diff |
| `PistonMovingBlockEntity` (Schleim-Stoß) | Bedingung 26.2 `!(entity instanceof ServerPlayer)`, 26.3 `canSimulateMovement()`. Für Windkugeln gleich | Diff |
| `ThrowableProjectile` | Blasensäule im ersten Tick: nur umgebaut (`findBlocksIn`), Verhalten gleich | Diff |
| Perlen-Teleport | 26.2 behält den Schwung, 26.3 setzt ihn auf null | [perlenkanone.md](perlenkanone.md) 3.4 |
| Perlen laden Chunks | erst **ab 1.21.2**. In 1.21.0/1.21.1 bleibt eine schnelle Perle am Rand der geladenen Chunks stehen. Für Schüsse über Tausende Blöcke ist der Bean Blaster dort *vermutlich* unbrauchbar | [perlenkanone.md](perlenkanone.md) 3.1. *Für 1.21.0/1.21.1 nicht im Code geprüft* |
| 1.21.0–1.21.10 | **nicht geprüft**. Laut Wiki-Historie seit 1.21 keine Änderung, die hier wichtig wäre | [Wiki](https://minecraft.wiki/w/Wind_Charge) |

**Hergeleitet, nicht getestet:** Ab 1.21.2 lädt die Perle die Chunks erst, *nachdem* sie in einen neuen Chunk gekommen ist (Ticket mit Radius 2). Bei Startgeschwindigkeiten von 15–300 Blöcken/Tick überspringt sie pro Tick viele Chunks. Sie wartet dann jeweils, bis ihr Chunk geladen ist. Auch das könnte die „about a minute“ erklären.

---

## 6. 6b6t (Folia/Fox)

**Regeln:** Es gibt keine Regel gegen Windkugeln, Spender, Redstone oder Perlen ([6b6t-regeln-limits.md](6b6t-regeln-limits.md) Abschnitt 1, [perlenkanone.md](perlenkanone.md) 5). Der Bean Blaster nutzt nur Vanilla-Mechanik, kein Exploit.

**Probleme (aus den Betreiber-Issues abgeleitet):**
- **Die Perlen-Kammer ist eine Stasis-Kammer.** Laut [Issue #7](https://github.com/6b6t/feedback/issues/7) (offen) verschwinden Stasis-Perlen etwa 2 s, nachdem der Besitzer teleportiert („/tp“, Dimensionswechsel, Wechsel des Workers). **Zwischen Einwurf und Landung (≈ 1 min) darf man also nicht teleportieren**, weder per `/home` noch per TPA.
- **Landung auf einem anderen Worker:** Laut Issue #7 verwirft Fox die Perle beim Aufprall, wenn der Besitzer „between workers“ ist („the pearl is already gone and the player is never teleported“). Ob ein Schuss über 5.000+ Blöcke einen Worker-Wechsel auslöst, ist **unbekannt**.
- **Chunk-Laden unter Folia:** Laut Issue #7 gibt `Projectile.getOwner()` unter Folia `null` zurück, „unless the owner is on the current tick thread“. In Vanilla setzt die Perle ihr Chunk-Ticket aber nur bei `owner instanceof ServerPlayer` ([perlenkanone.md](perlenkanone.md) 3.1). **Hergeleitet, nicht verifiziert:** Verlässt die Perle die Region des Spielers, lädt sie auf 6b6t womöglich keine Chunks mehr und bleibt am Rand der geladenen Chunks hängen.
- **Ankunft in einem Gebiet, das noch lädt:** Das kann einen Kick auf den Backup-Server auslösen ([Issue #28](https://github.com/6b6t/feedback/issues/28), offen).
- **Entity-Limits:** Bekannt ist nur „ender_pearl removal limit 100 per chunk“ (Fox `EntityLimits`, [Issue #7](https://github.com/6b6t/feedback/issues/7)). Ob es ein Limit für `wind_charge` gibt, ist **unbekannt**. Die Speicher halten Hunderte Windkugel-Entities in einem Chunk. In Paper ist `chunks.entity-per-chunk-save-limit` nur für `experience_orb` vorbelegt, `wind_charge` steht dort nicht ([Paper-Doku](https://docs.papermc.io/paper/reference/world-configuration/)). Die 6b6t-Konfiguration ist nicht öffentlich.
- **Version:** 6b6t läuft auf 26.2. Die Spiellogik ist dort gleich (Abschnitt 5). Auf 26.2 behält der Spieler nach der Landung seinen Schwung.

**Urteil:** Erlaubt ja. Auf 6b6t aber **unzuverlässig**, aus denselben Gründen wie die TNT-Kanone: Stasis-Bug, Worker-Wechsel, Chunk-Ticket unklar. Ein Test über kurze Strecken (≤ 1.000 Blöcke, gleiche Region) wäre der erste Schritt.

---

## 7. Kosten

1 Breeze-Rute = 4 Windkugeln (Rezept `wind_charge.json`: 1 `breeze_rod` → 4). Eine Breeze lässt 1–2 Ruten fallen (Schnitt 1,5), mit Plünderung III 4–8 (Schnitt 6), aber nur, wenn ein Spieler oder ein gezähmter Wolf sie tötet ([Wiki: Breeze Rod](https://minecraft.wiki/w/Breeze_Rod)).

Ergebnisse des Mod-Rechners (Python-Port), Ziel relativ zum Magmablock:

| Ziel | Windkugeln | Breeze-Ruten |
|---|---|---|
| 1.000 entlang X (±) oder +Z | 29 (27 + 2) | 8 |
| 1.000 nach −Z | 33 | 9 |
| 1.000 diagonal (⟨707, 707⟩) | 39 | 10 |
| 5.000 entlang X oder +Z | 149 | 38 |
| 5.000 nach −Z | 171 | 43 |
| 5.000 diagonal | 197 | 50 |
| 10.000 entlang X oder +Z | 299–300 | 75 |
| 10.000 nach −Z | 342 | 86 |
| 10.000 diagonal | 394–454 (je nach Quadrant) | 99–114 |

Dazu kommt die Reserve in den Trichtern: etwa 5 % Verlust laut Video, und der Erfinder empfiehlt „a stack or two“ pro Trichter. Verbraucht wird davon nur, was wirklich verloren geht. **Faustregel:** etwa 3 Windkugeln (≈ 0,75 Ruten) pro 100 Blöcke entlang einer Achse. Diagonal kostet es bis zu ≈ 40 % mehr, weil sich die Kosten nach |ΔX| + |ΔZ| richten und nicht nach der Luftlinie.

---

## 8. Was Cheecky machen könnte (nur Vanilla-Eingaben)

Grundsatz wie in [perlenkanone.md](perlenkanone.md) 4: nur Eingaben, die ein Spieler auch selbst machen kann. Keine erfundenen Pakete, keine Umgehung.

1. **Rechner-Befehl** (z. B. `.bb setup` / `.bb <x> <z>` im vorhandenen `.`-Befehlssystem, `Commands.java`). Die Befehle werden lokal ausgeführt und nie an den Server geschickt:
   - Formel aus Abschnitt 4 übernehmen (MIT-Lizenz, Quelle nennen). Das Ergebnis besser **runden** statt abschneiden, Warnung bei > 576.
   - Optional die vier Spender-Vektoren selbst einmessen: N Windkugeln schießen, Landepunkt merken, Vektor = Δ/N. Das hilft bei eigenen Nachbauten und bei der 31/36-Unklarheit.
   - Korrekturfaktor für die Bodenhöhe am Ziel aus 3.2 anbieten (Höhe aus Karte oder Eingabe).
   - Magmablock-Position pro Server und Dimension speichern, wie in `Homes.java`. Prüfen, ob der Block auf Y 250 steht.
   - Kosten in Stacks, Shulkern und Breeze-Ruten anzeigen. Das Ziel als Wegpunkt bzw. Baritone-Ziel übernehmen (`BaritoneBridge`).
2. **Bestücken:** Spender öffnen (`useItemOn`) und die genaue Anzahl per normaler Menü-Klicks hineinlegen (`InvUtil`), mit menschlichen Pausen.
3. **Kontrolle:** Windkugel-Entities in der Nähe der Speicher zählen und die Perle in der Kammer erkennen. Beides sieht der Client (`clientTrackingRange 4`, `updateInterval 10`), aber nur grob. Melden: „geladen: 124/124“.
4. **6b6t-Schutz:** Solange die eigene Perle in der Kammer liegt oder fliegt, vor `/home`, TPA und Dimensionswechsel warnen (Issue #7).
5. **Bauhilfe:** Die genaue Geometrie ist nicht bekannt. Besser auf IddieBeans `.litematic` mit Litematica verweisen, statt eine eigene Bauhilfe zu bauen.

---

## 9. Was nicht gefunden wurde

- **Blockliste und Maße:** Planet Minecraft (Welt, `.litematic`, Python-Code) und beanblaster.tems.pl waren nicht abrufbar (403 bzw. Cloudflare-Prüfung). Daher gibt es keine Stückzahlen, keinen Schaltplan und keine Abstände Windkugel ↔ Perle.
- **Videos nur als Transkript gelesen.** Bilder nicht gesehen. Das automatische Transkript kann Wörter falsch erkannt haben, z. B. „Y axis“ statt Z.
- **Python-Original nicht gelesen.** Daher unklar, ob 31/36 je nach Sektor ein Fehler ist (Abschnitt 4).
- **Codepfad „Windkugel stoppt am bewegten Kolbenkopf“** nicht nachverfolgt, nur das Wiki-Zitat.
- **EthosLabs Original-Speicher** (Video, Bauweise) nicht gefunden.
- 1.21.0–1.21.10 nicht im Code geprüft, nur 1.21.11, 26.2 und 26.3.
- Keine Messung im Spiel. Alle Zahlen in 3.1, 3.2 und 7 sind hergeleitet, simuliert oder kommen aus dem Rechner.
- 6b6t: Limit für `wind_charge` unbekannt, Chunk-Ticket fliegender Perlen unter Folia unbekannt, Größe der Worker-Bereiche unbekannt.

---

## Kernzahlen

| Größe | Wert | Quelle |
|---|---|---|
| Stoß pro Windkugel | `(1 − d/2.4) · exposure · 1.22` Blöcke/Tick. Rückgerechnet aus den Rechner-Werten: ≈ 0,53–0,62 | `ServerExplosion`, `WindCharge`; 3.2 |
| Weite pro Windkugel | 31–36 Blöcke (empirisch, Bau auf Y 250, Boden Y 82) | Mod-Code, Video |
| Windkugeln untereinander | keine Kollision, kein Rückstoß (`push` leer) → beliebig viele am selben Punkt | `AbstractWindCharge` |
| Perle halten | Seelensand- und Magmasäule nebeneinander: +0,06 − 0,03 − 0,03 (Schwerkraft) = 0 | `Entity.handleOnInsideBubbleColumn`, Video |
| Spender | 1 Windkugel pro Auslösung, ≈ 1 pro 4 Ticks, max. 576 Stück | `DispenserBlock`, hergeleitet |
| Kosten | ≈ 3 Windkugeln pro 100 Blöcke entlang der Achse; 10.000 Blöcke ≈ 300 Windkugeln = 75 Ruten (diagonal ≈ 400–450 = 100–114 Ruten) | 7 |
| Genauigkeit | ≈ 80–150 Blöcke Abweichung (Erfinder); ±3–7 % durch die Bodenhöhe | Video 2; 3.2 |
| 6b6t | erlaubt; Stasis- und Worker-Bug (#7) machen es riskant | [Issue #7](https://github.com/6b6t/feedback/issues/7) |
