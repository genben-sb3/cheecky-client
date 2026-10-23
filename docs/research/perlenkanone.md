# Kleine TNT-Enderperlen-Kanone (MC 26.2/26.3) – Recherche

Stand: 2026-10-23. Ziel: verstehen, wie eine kleine, kompakte TNT-Perlenkanone in Minecraft Java 26.2/26.3 funktioniert, die per Knopf oder Hebel ausgelöst wird, und was der Cheecky Client (MC 26.3) daran automatisieren kann. Außerdem geprüft: Steht auf 6b6t etwas dagegen?
**Nicht im Umfang:** Anti-Cheat-Umgehung, Server-Exploits und Dupes, also auch keine TNT-Duper. Große 360°-Kanonen werden nur erwähnt, weil sie fast immer mit TNT-Dupern betrieben werden ([Wiki: 360 degree ender pearl cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).

Alle Quellen wurden am **2026-10-23** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Spiel-Jars 26.3 (`minecraft-merged-7e9a32a5b8-26.3.jar`) und 26.2 (`minecraft-merged-201bed67e3-26.2.jar`) aus `.gradle/loom-cache`, Mojang-Namen, gelesen mit `javap -c -p` | Spielcode | Primärquelle |
| [minecraft.wiki: Explosion](https://minecraft.wiki/w/Explosion), [TNT](https://minecraft.wiki/w/TNT), [Ender Pearl](https://minecraft.wiki/w/Ender_Pearl), [Java Edition 1.21.2](https://minecraft.wiki/w/Java_Edition_1.21.2) | Wiki | Primärquelle (Community, mit Code abgeglichen) |
| [minecraft.wiki: Tutorial:360 degree ender pearl cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon), [Tutorial:TNT cannons](https://minecraft.wiki/w/Tutorial:TNT_cannons) | Wiki-Tutorial | Primärquelle für Bauprinzipien |
| [TNT Archive Resources (lntricate1)](https://gist.github.com/lntricate1/e7a2c3f0e10eaa40ac932cab74619ea7), Nachfolger von [pwouiks Gist](https://gist.github.com/pwouik/2c7ffd4e0f8ac96f4e115dedde46e84a) | Sammlung der technischen Community | Sekundär, verweist auf Originalvideos |
| Videos von Xcom6000, Fallen_Breath, Nugglz u. a. (Titel/Autor per YouTube-oEmbed geprüft) | Videos | **Inhalt nicht angesehen**, nur als Hinweis gelistet |
| [6b6t.org/en/rules](https://6b6t.org/en/rules), [ToS](https://6b6t.org/en/terms-of-service), [github.com/6b6t/feedback](https://github.com/6b6t/feedback) (Issues #7, #9, #28) | Betreiber | offiziell (Issues = interne Arbeitsnotizen) |

Physik-Code ist in 26.2 und 26.3 gleich. Geprüft per Diff des normalisierten Bytecodes von `ServerExplosion`, `PrimedTnt`, `ThrowableProjectile`, `ThrownEnderpearl`, `ExplosionDamageCalculator` und `TicketType`. Unterschiede gibt es nur bei Sprungadressen, bei `PrimedTnt.setRequiresPrecisePosition` (Netzwerk-Genauigkeit, neu in 26.3), bei der Blasensäulen-Prüfung im ersten Tick und beim Teleport (siehe 3.4).

---

## 1. Physik aus dem Spielcode

### 1.1 Wie eine Explosion Entities schiebt (`ServerExplosion.hurtEntities`)

Aus dem Bytecode von `net.minecraft.world.level.ServerExplosion.hurtEntities()` (26.3):

1. **Reichweite:** `r2 = radius * 2`. Gesucht wird in einer AABB von `center ± r2 ± 1` (`ServerLevel.getEntities(source, aabb)`).
2. Entities mit `ignoreExplosion(explosion) == true` werden übersprungen.
3. `dist = sqrt(entity.distanceToSqr(center)) / r2`. Ist `dist > 1`, wird die Entity übersprungen. `distanceToSqr` misst zur **Entity-Position** (Füße).
4. **Richtung:** `normalize(P − center)`. Für `PrimedTnt` ist `P` die Position (Füße), für alle anderen Entities `getEyePosition()`. `Vec3.normalize()` gibt `Vec3.ZERO` zurück, wenn die Länge < 1e‑5 ist (Bytecode `Vec3.normalize`).
5. `exposure = getSeenPercent(center, entity)`. Das wird nur berechnet, wenn `shouldDamageEntity` gilt oder `knockbackMultiplier ≠ 0`.
6. `knockbackResistance = EXPLOSION_KNOCKBACK_RESISTANCE`, aber nur für `LivingEntity`, sonst 0.
7. **Stoß:** `k = (1 − dist) * exposure * knockbackMultiplier * (1 − knockbackResistance)`, danach `entity.pushFromExplosion(direction * k)`.
8. `Entity.pushFromExplosion` ruft `push(vec)` auf: `deltaMovement += vec`. Es gibt **keine Obergrenze**, geprüft wird nur `Double.isFinite` (Bytecode `Entity.push(DDD)`).

Der Standard-`ExplosionDamageCalculator` liefert `shouldDamageEntity = true` und `getKnockbackMultiplier = 1.0` (Bytecode). Die Formel stimmt mit der im Wiki überein: „(1 - distance/(2*power)) * exposure * knockbackMultiplier * (1 - EXPLOSION_KNOCKBACK_RESISTANCE)“ ([Wiki: Explosion](https://minecraft.wiki/w/Explosion)).

**Reihenfolge in `ServerExplosion.explode()`:** zuerst `calculateExplodedPositions()`, dann `hurtEntities()`, dann `interactWithBlocks()` und `createFire()` (Bytecode). Blöcke, die die Explosion zerstört, verdecken die Perle also beim Schieben noch.

### 1.2 Exposure (`getSeenPercent`)

Gitter über die Hitbox: Schrittweite `1/(Breite*2+1)` je Achse, mit X/Z-Versatz zur Zentrierung. Von jedem Gitterpunkt aus wird ein Strahl zum Explosionszentrum geprüft, mit `ClipContext.Block.COLLIDER` und `ClipContext.Fluid.NONE`. Ergebnis ist der Anteil der Strahlen ohne Treffer (Bytecode `ServerExplosion.getSeenPercent`).
Folgen:
- **Wasser und Lava blockieren den Stoß nicht** (`Fluid.NONE`). Feste Kollisionsformen (auch Glas) blockieren ihn.
- Die Perle ist 0,25 × 0,25 groß (`EntityTypes.ENDER_PEARL`, `sized(0.25f, 0.25f)`). Schrittweite: 1/1,5. Damit gibt es 2 × 2 × 2 = 8 Strahlen, und die Exposure ist ein Vielfaches von 1/8.

### 1.3 Gezündetes TNT (`PrimedTnt`)

- Zündschnur **80 Ticks** (`setFuse(80)`, `DEFAULT_FUSE_TIME`). Explosionsstärke **4.0** (`DEFAULT_EXPLOSION_POWER`). Reichweite für Entities also **8 Blöcke**.
- **Anfangssprung beim Zünden:** `deltaMovement = (−sin(a)·0.02, 0.2, −cos(a)·0.02)` mit zufälligem Winkel `a` (Konstruktor `PrimedTnt(Level, x, y, z, owner)`). Der Spender benutzt denselben Konstruktor und setzt das TNT auf die Mitte des Blocks vor sich, `(x+0.5, y, z+0.5)` (`DispenseItemBehavior$6`). Laut Wiki: „initial velocity of 0.2 blocks per tick upward, and 0.02 blocks per tick in a random direction“ ([Wiki: TNT](https://minecraft.wiki/w/TNT)).
- Tick-Reihenfolge: Schwerkraft (0.04) → `move` → Luftwiderstand (`getAirDrag`) → am Boden `× (0.7, −0.5, 0.7)` → Zündschnur −1 → bei 0 `explode()`. TNT **bewegt sich also noch, bevor es explodiert** (Bytecode `PrimedTnt.tick`).
- Explosionszentrum: `getY(0.0625)`, also Position + 0,98 · 0,0625 = **+0,06125** über den Füßen (Bytecode; TNT-Größe 0,98 × 0,98 in `EntityTypes`; bestätigt in [Wiki: TNT](https://minecraft.wiki/w/TNT)).
- Ob TNT überhaupt explodiert, steuert die Spielregel `TNT_EXPLODES` (`tntExplodes`, seit 1.21.5 / 25w10a laut [Wiki: TNT](https://minecraft.wiki/w/TNT)). Ob Blöcke zerstört werden, steuert `TNT_EXPLOSION_DROP_DECAY` (`ServerLevel.explode`).
- **In Wasser:** Im Code gibt es keinen eigenen Wasser-Sonderfall. Die Blockstrahlen nutzen `getBlockExplosionResistance(…, FluidState)`. Steht das TNT in Wasser, wird der Strahl sofort gestoppt, und es gehen keine Blöcke kaputt. Laut Wiki gilt: „Explosions that occur in flowing water or lava apply propulsion and damage to entities“, Blöcke bleiben aber heil ([Wiki: Explosion](https://minecraft.wiki/w/Explosion)). Der Stoß auf die Perle bleibt also voll erhalten (siehe 1.2).

### 1.4 Die geworfene Perle (`ThrowableProjectile` / `ThrownEnderpearl`)

- `getDefaultGravity() = 0.03`, `getAirDrag() = 0.99f`, **in Wasser 0.8f** (`ThrowableProjectile.applyInertia`).
- Tick-Reihenfolge: Schwerkraft → Luftwiderstand → Bewegung (`ThrowableProjectile.tick`: `applyGravity`, `applyInertia`, dann `setPos(position + delta)`). Das Wiki nennt dieselbe Reihenfolge („tickingorder = adp“) ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).
- **Perlen werden von Explosionen geschoben:** Weder `ThrownEnderpearl` noch `ThrowableItemProjectile`, `ThrowableProjectile` oder `Projectile` überschreiben `ignoreExplosion` oder `pushFromExplosion` (Bytecode durchsucht). Es gilt also `Entity.ignoreExplosion = false`. `Projectile.hurtServer` gibt immer `false` zurück, die Perle nimmt keinen Schaden. Laut Wiki: „Explosions can redirect projectiles, including ender pearls.“ ([Wiki: Explosion](https://minecraft.wiki/w/Explosion)).
- Die Perle ist nicht im Tag `REDIRECTABLE_PROJECTILE`, der Besitzer wechselt also nicht. Das wird nur für Projektile mit diesem Tag gemacht (Bytecode `hurtEntities`, Zeile 426 ff.).
- Augenhöhe: In `EntityTypes` ist keine eigene `eyeHeight` gesetzt. Damit gilt der Standard des Builders (0,85 × Höhe = 0,2125). *Diesen Standardwert habe ich nicht im Bytecode von `EntityType$Builder` nachgeprüft.*
- Die Perle kollidiert mit Entities, auch mit gezündetem TNT. Trifft sie eins, landet sie dort. Deshalb darf das TNT laut Wiki höchstens bis **0,79 Blöcke** an die Perle heran ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon), „because of the ender pearl's collision checks with TNT, the closest the TNT can go is 0.79 blocks away from the center“).
- Eine Geschwindigkeitsgrenze gibt es nicht (siehe 1.1, `push`). Die Endgeschwindigkeit im freien Fall beträgt 2,97 Blöcke/Tick ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl)).
- Wurf von Hand: Geschwindigkeit 1,5 (Konstante `1.5f` in `EnderpearlItem`), Abklingzeit 20 Ticks ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl)).

### 1.5 Hergeleitet: Geschwindigkeit pro TNT

Aus 1.1 mit Stärke 4 (r2 = 8), Multiplikator 1 und Resistenz 0 (die Perle ist keine `LivingEntity`):

```
Δv = (1 − d/8) · exposure   [Blöcke/Tick]
d = Abstand Explosionszentrum (TNT-Füße + 0,06125) → Perlen-Füße
Richtung = normalize(Perlen-Auge − Explosionszentrum)
```

| Abstand d | Δv pro TNT bei voller Exposure |
|---|---|
| 0,79 (Minimum laut Wiki) | 0,901 |
| 1,0 | 0,875 |
| 1,5 | 0,812 |
| 2,0 | 0,750 |
| 3,0 | 0,625 |
| 4,0 | 0,500 |

Zum Vergleich: Das Wiki-Beispiel für ein perfekt ausgerichtetes 4-Ecken-Basket nennt `0.6025927` je Achse (X und Z), also 0,852 Blöcke/Tick pro TNT ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon), Rechner-Standardwerte).

**Mehrere TNT:** Jede Explosion addiert ihren eigenen Vektor zur Perlen-Geschwindigkeit (1.1, Schritt 8). Laut Wiki: „the velocity given by an explosion to an entity is always identical and is added to the entity's existing velocity“ ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)). N TNT am selben Punkt ergeben N · Δv, solange das TNT dort auch **wirklich** explodiert. Der Haken (hergeleitet aus 1.1 und 1.3): Jede Explosion schiebt auch das restliche TNT, und mit Abstand ≈ 0 ist das fast 1 Block/Tick. Weil TNT sich vor dem Explodieren noch bewegt (1.3), explodiert verschobenes TNT woanders. Nur TNT an *exakt* derselben Position wird nicht geschoben (`normalize` → `ZERO`). Deshalb richten große Kanonen das TNT genau aus ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon), „All the main TNT is aligned into the exact same position“). Eine kleine Kanone mit Spendern und Zufallssprung erreicht das nicht. Sie streut deshalb stärker.

### 1.6 Hergeleitet: Reichweite

Simuliert mit der Tick-Reihenfolge aus 1.4 (eigenes Skript, keine Messung im Spiel). Landung auf gleicher Höhe wie der Start, Luft ohne Hindernisse:

| TNT | Startgeschw. (0,79 Abstand) | max. flach (optimaler Winkel) | Schuss ab 30° (z. B. TNT 2 seitlich, 1 tiefer) |
|---|---|---|---|
| 1 | 0,90 | ≈ 21 Blöcke | ≈ 13 Blöcke (Δv 0,72) |
| 4 | 3,6 | ≈ 200 | ≈ 140 |
| 8 | 7,2 | ≈ 520 | ≈ 390 |
| 16 | 14,4 | ≈ 1.230 | ≈ 930 |
| 32 | 28,8 | ≈ 2.670 | – |

- Grenzwert: Eine Perle legt höchstens **99 Blöcke pro Block/Tick** Startgeschwindigkeit zurück ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl); folgt aus 1/(1−0,99)). Bei 0,9 Blöcke/Tick pro TNT sind das **≈ 89 Blöcke pro TNT** als obere Schranke. Auf flachem Boden bringt jedes weitere TNT zwischen 4 und 16 TNT etwa **60–90 Blöcke**.
- Liegt die Perle beim Stoß in Wasser (z. B. in einer Blasensäule), gilt im ersten Tick Luftwiderstand 0,8 statt 0,99 (1.4). Das kostet grob 20 % der Geschwindigkeit. *Hergeleitet, nicht gemessen.* Das Wiki sagt dazu nur, dass Wasser „changes the ender pearl's movement“, und nennt Lava als häufigere Wahl ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).
- **Richtung** = Vektor vom TNT zur Perle. Die Höhe bestimmt den Abschusswinkel: Liegt das TNT tiefer, fliegt die Perle steiler. **Entfernung** = Anzahl TNT, bei fester Geometrie. Für feine Korrekturen kann man auch den Abstand ändern (Tabelle in 1.5).

---

## 2. Bauweisen aus der Community

### 2.1 Verifiziert (Primärquelle gelesen)

- **Bauteile einer Perlenkanone** laut Wiki ([Tutorial:360 degree ender pearl cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)):
  - ein „ender pearl aligner“, der die Perle zuverlässig an eine exakte Stelle bringt
  - eine Explosionsquelle, „usually an array of TNT dupers, but TNT dispensers, ghast fireballs, and wind charges are used as well“
  - ein „basket“, der das TNT an feste Stellen neben der Perle bringt
  - optional ROM und Bedienfeld

  Der Artikel beschreibt nur das Basket, keinen kompletten Bauplan.
- **Basket-Details (verifiziert):** Das TNT steht in Blockecken, mindestens 0,79 Blöcke von der Perle entfernt. Die Blöcke an den Ecken dürfen höchstens 11 Pixel Hitbox haben (Blumentopf, Conduit, Mauer ohne Mittelsäule). „Lava is most commonly used to stop the explosions from destroying blocks. Water also works, but unlike lava it cannot be used in the Nether and changes the ender pearl's movement.“ Das TNT steht auf Zäunen unter der Lava, weil die Strahlen den oberen Teil von Zäunen nicht treffen. Jedes Eckenpaar erreicht nur einen Quadranten ([Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).
- **TNT-Anzahl berechnen (verifiziert):** Die Explosionsvektoren bilden die Spalten einer Matrix M. Die TNT-Anzahl ist die Pseudoinverse M⁺ mal Zielgeschwindigkeit, gerundet. Die nötige Startgeschwindigkeit für t Ticks: `v0 = (1−d)/(d(1−d^t)) · (s − d·t/(1−d)·a) + d/(1−d)·a` mit `d = 0.9900000095367432` und `a = (0, −0.03, 0)`. Beispiel: ⟨20, 0, 10⟩ → 25 und 8 TNT ([Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)). Wichtig: Die Formel nutzt **v = Geschwindigkeit**, nicht Weite.
- **Perle schwebend lagern (verifiziert):** „An ender pearl can remain afloat on top of an upward bubble column, allowing it to be stored indefinitely.“ Das gilt nur in Java ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl)). Im Code überschreibt `ThrownEnderpearl` dafür `onAboveBubbleColumn` und `onInsideBubbleColumn`.
- **Bekannte große Kanonen (Wiki-Tabelle):**
  - „360 FTL“ (1.12, Xcom6000 u. a., März 2019)
  - „360 FTL HEAVY“ (1.13.2–1.21.1, Video von Fallen_Breath)
  - „836 FTL“ und „836 FTL v2“ (1.21.2+, nur im TMC-Discord)

  Alle haben ein 4-Ecken-, Differential- oder Kreis-Basket und laufen mit Dupern. Sie sind **nicht klein** ([Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)).

### 2.2 Kleine Bauweise – aus Code und Wiki abgeleitet, **nicht im Spiel getestet**

Eine verifizierte, dokumentierte *kleine* Bauweise für 1.21+/26.x mit Blockliste habe ich in den Primärquellen **nicht** gefunden (siehe Abschnitt 6). Aus den Bausteinen in 2.1 und der Physik in Abschnitt 1 folgt dieses Minimalprinzip für **eine feste Richtung**:

| Teil | Blöcke (Vorschlag) | Zweck | Grundlage |
|---|---|---|---|
| Perlen-Halter | Seelensand + 1–2 Wasserquellen darüber (Blasensäule nach oben) | Die Perle schwebt reglos oben auf der Säule und wartet auf den Schuss | [Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl) |
| TNT-Kammer | 1 Wasser- oder Lavaquelle neben/unter der Perle, **ohne festen Block dazwischen** | Keine Blockschäden. Flüssigkeit verdeckt den Stoß nicht (`Fluid.NONE`) | 1.2, 1.3, [Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon) |
| TNT-Quelle | 1–n Spender, die in die Kammer zeigen, mit TNT gefüllt | Jeder Puls zündet 1 TNT pro Spender. Die Anzahl der Spender bestimmt die Weite | 1.3 (`DispenseItemBehavior$6`) |
| Auslöser | Knopf oder Hebel + Redstone zu allen Spendern | Alle Spender im selben Tick zünden | – |
| Abschirmung | Feste Blöcke nur *außerhalb* der Strahlen zwischen TNT und Perle | Exposure = 1 | 1.2 |

- Richtung: Das TNT steht auf der *Gegenseite* des Ziels. Steht es eine Ebene tiefer, ergibt das etwa 30–50° Abschusswinkel (Tabelle 1.6).
- Weite: Anzahl gleichzeitig feuernder Spender. Bei 1–2 Blöcken Abstand ergibt jedes TNT 0,75–0,87 Blöcke/Tick. In der Praxis streut die Weite (Zufallssprung beim Zünden, TNT schiebt TNT, 20 % Wasserverlust; siehe 1.5, 1.6).
- Nach 80 Ticks Zündschnur explodiert das TNT, die Perle fliegt, und der Spieler wird am Landepunkt teleportiert, egal wo er gerade steht (2.1, 3.1).

### 2.3 Nicht verifiziert (nur gefunden, Inhalt nicht geprüft)

- Videos „Minecraft Ender Pearl Cannon V2 (Full Tutorial) - Java Only“ und „Minecraft's MOST OP Ender Pearl Cannon (Full Tutorial)“ von *Nugglz* ([fySWaBWxQZ4](https://www.youtube.com/watch?v=fySWaBWxQZ4), [yHEjCCLwYtc](https://www.youtube.com/watch?v=yHEjCCLwYtc)), „POWERFUL Ender Pearl Cannon TUTORIAL“ von *Vurge* ([tBxYrlUCBBc](https://www.youtube.com/watch?v=tBxYrlUCBBc)). Größe, Version und Reichweite sind unbekannt, weil die Beschreibung nicht abrufbar war.
- Grundlagen-Serie von Xcom6000: „Chunk Loading (1/3)“, „Explosions (2/3)“, „Pearl Alignment (3/3)“ und „History of Pearl Cannons“ ([Gist-Liste](https://gist.github.com/lntricate1/e7a2c3f0e10eaa40ac932cab74619ea7), dort „accurate to mc1.12“). Diese Videos sind die Primärquelle der technischen Community, wurden aber nicht angesehen.
- Planet-Minecraft-Projekte („Compact Ender Pearl Cannon“) und ein Instructables-„Ender Pearl Launcher“ ohne TNT, mit Schleimblöcken, 75 Blöcke Reichweite. Das sind keine Primärquellen und sie sind nicht geprüft.

---

## 3. Änderungen seit 1.21

### 3.1 Perlen laden Chunks (1.21.2)

- Changelog 1.21.2: „Thrown ender pearls now load and tick chunks around them … An ender pearl creates a ticket for chunk loading any time it moves into a new chunk, and also periodically every 10 seconds.“ Außerdem: „Now unload when a player logs out, and reappear whenever that player logs back in, similar to how mounts currently behave.“ ([Java Edition 1.21.2](https://minecraft.wiki/w/Java_Edition_1.21.2)).
- **Code 26.3 weicht vom Changelog ab:** `TicketType.ENDER_PEARL` wird mit Timeout **40 Ticks** registriert, `ServerPlayer.placeEnderPearlTicket` mit Radius **2** (`addTicketWithRadius(ENDER_PEARL, chunkPos, 2)`). `ThrownEnderpearl.tick` erneuert das Ticket, sobald die Perle in einen neuen Chunk kommt oder der Timer abläuft. Das passt zum aktuellen Wiki-Artikel: „every time it moves … into a new chunk, and also periodically almost every 2 seconds (39 ticks)“, mit 5 × 5 Chunks ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl)).
- Nur Perlen, deren Besitzer ein `ServerPlayer` ist, laden Chunks (`ThrownEnderpearl.tick`: `instanceof ServerPlayer` → `registerAndUpdateEnderPearlTicket`). Laut Wiki gilt das, „as long as the player who threw it is online, even when that player is in a different dimension“ ([Wiki](https://minecraft.wiki/w/Ender_Pearl)).
- **Folge für Kanonen:** Eine Perle mit beliebig hoher Geschwindigkeit fliegt in Vanilla weiter, weil sie die Chunks vor sich selbst lädt. In 1.12-Kanonen war ein eigenes Chunkloading nötig, das Thema von Xcoms Video 1/3 ([Gist](https://gist.github.com/lntricate1/e7a2c3f0e10eaa40ac932cab74619ea7)).

### 3.2 Verhalten beim Ausloggen

- `ServerPlayer.saveEnderPearls` und `loadAndSpawnEnderPearls` (NBT-Schlüssel `ender_pearls`) speichern fliegende Perlen mit dem Spieler. Beim Login werden sie wieder erzeugt, mit neuem Ticket. `ThrownEnderpearl.onRemoval(UNLOADED_WITH_PLAYER)` meldet die Perle ab (Bytecode 26.3). Bestätigt durch den 1.21.2-Changelog ([Wiki](https://minecraft.wiki/w/Java_Edition_1.21.2)).
- Stirbt der Besitzer, verschwindet die Perle, wenn `ender_pearls_vanish_on_death` an ist (Standard). Siehe `ThrownEnderpearl.tick` und [Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl).

### 3.3 TNT

- 1.21: TNT geht durch Netherportale. 1.21.2 (24w33a): Feld `explosion_power` neu. 1.21.5: Standard-`fuse` 80 auch bei `/summon`, Spielregel `tntExplodes` neu. 1.21.6: TNT in Wasser zerstört wieder Items und Rüstungsständer ([Wiki: TNT](https://minecraft.wiki/w/TNT)).
- 26.2: Schwefelwürfel können TNT aufnehmen ([Wiki: TNT](https://minecraft.wiki/w/TNT)). Für die Kanone ist das unwichtig.
- An der Knockback-Formel hat sich laut Wiki seit 1.21 nichts geändert ([Wiki: Explosion](https://minecraft.wiki/w/Explosion)). Im Code 26.2 und 26.3 ist sie gleich (Diff, siehe oben).

### 3.4 Perlen in 26.3

- Projektile kollidieren jetzt mit der Weltgrenze wie mit einem festen Block ([Wiki: Ender Pearl](https://minecraft.wiki/w/Ender_Pearl), Eintrag 26.3).
- **Neu gefunden (Bytecode-Diff):** In 26.2 übergibt der Perlen-Teleport `Relative.union(ROTATION, DELTA)`, der Spieler **behält seinen Schwung**. In 26.3 übergibt er nur `Relative.ROTATION` mit `Vec3.ZERO`, die Geschwindigkeit des Spielers ist nach dem Teleport **null**. Im Wiki habe ich dazu nichts gefunden.

---

## 4. Was der Client automatisieren kann (nur Vanilla-Interaktionen)

Der Client schickt nur das, was ein Spieler auch von Hand tun kann. Die passenden Aufrufe gibt es schon im Projekt:

| Schritt | Vanilla-Aufruf (26.3) | Schon im Projekt genutzt |
|---|---|---|
| Hotbar-Slot wählen | `Inventory.setSelectedSlot(int)` | `ElytraFly.java:83`, `AutoCrystal.java:166` |
| Blöcke setzen (Seelensand, Wasser per Eimer, Spender, Knopf, Redstone) | `MultiPlayerGameMode.useItemOn(player, hand, BlockHitResult)` | `AutoCrystal.java:124`, `AutoAnchor.java:172` |
| Spender mit TNT füllen | Container öffnen (`useItemOn` auf Spender) + Klicks im Menü | – |
| Perle in die Blasensäule werfen | Blick auf die Säule richten + `MultiPlayerGameMode.useItem(player, hand)` | `PearlAim.java:94` |
| Knopf drücken / Hebel umlegen | `useItemOn` auf den Knopf | wie oben |

Sinnvolle Bausteine für ein Modul „PearlCannon“ (Vorschlag):

1. **Rechner:** Aus Ziel und Kanonen-Geometrie die TNT-Anzahl berechnen, mit der Formel aus 1.5 und der v0-Formel aus 2.1, und die Flugbahn wie bei `PearlAim` anzeigen. Die Δv pro TNT kann man einmal einmessen: das Ergebnis eines Schusses beobachten und daraus Δv ableiten.
2. **Bauhilfe:** ein festes Muster (Tabelle 2.2) relativ zum Spieler Block für Block aus der Hotbar setzen.
3. **Schuss-Ablauf:** Perle in die Säule werfen → warten, bis sie schwebt (der Client sieht das Perlen-Entity) → Knopf drücken → 80 Ticks Countdown anzeigen.

Grenzen:
- Das Perlen-Entity hat `updateInterval(10)` und `clientTrackingRange(4)` (`EntityTypes`). Der Client sieht die Position also nur grob, die Ansicht ist kein exakter Messwert.
- Die Explosion und den Stoß berechnet allein der Server.

---

## 5. 6b6t

**Offizielle Regeln:** Die Regel- und ToS-Seiten erwähnen **TNT, Redstone, Entities, Perlen und Lag-Machines nicht** (neu abgerufen am 2026-10-23; Suche nach „tnt“, „redstone“, „pearl“, „explos“, „entit“ ohne Treffer zu Spielinhalten; [Rules](https://6b6t.org/en/rules), [ToS](https://6b6t.org/en/terms-of-service)). Es gelten nur die drei Verbote und die Klausel „unusable for others“ (siehe [6b6t-regeln-limits.md](6b6t-regeln-limits.md), Abschnitt 1).

**Interne Betreiber-Notizen (öffentlich, aber keine Regeln):**
- **Perlen-Limit:** Fox hat `EntityLimits` mit „ender_pearl removal limit 100 per chunk“ ([Issue #7](https://github.com/6b6t/feedback/issues/7)). Für eine Kanone mit einer Perle ist das unerheblich. Ein Limit für TNT oder Redstone wird in keinem Issue genannt (Suche nach „tnt“, „redstone“, „explosion“, „primed“, „cannon“, „lag“ ohne Treffer, [Issue-Tracker](https://github.com/6b6t/feedback/issues)).
- **Perlen hängen an Folia (wichtig):** Laut Issue #7 (offen) sind auf 6b6t `ServerPlayer.registerEnderPearl` und `deregisterEnderPearl` No-ops: „pearls are never saved with the player, never unloaded with the player on quit, and never reloaded on login“. Stasis-Perlen verschwinden etwa 2 Sekunden, nachdem der Besitzer teleportiert. Landet eine Perle, während der Besitzer zwischen zwei „Workern“ wechselt, geht sie verloren ([Issue #7](https://github.com/6b6t/feedback/issues/7)). Das Vanilla-Verhalten aus 3.2 gilt auf 6b6t also **nicht**.
- **Chunkloading durch die Perle:** In Vanilla ruft `registerAndUpdateEnderPearlTicket` zuerst `registerEnderPearl` auf und setzt danach das Ticket (Bytecode). Ob Folia/Fox das Ticket trotzdem setzt, steht in Issue #7 **nicht**. Unbekannt. Ohne Ticket bleibt eine Perle an der Grenze zu nicht geladenen Chunks stehen. *Nicht verifiziert.*
- **Ankunft in ladenden Gebieten:** Spieler werden auf den Backup-Server geworfen, wenn sie in einem Gebiet ankommen, das noch lädt (offen, [Issue #28](https://github.com/6b6t/feedback/issues/28)). Ein langer Perlenschuss in ungeladenes Gebiet kann das auslösen. *Nicht für Perlen getestet.*

**Urteil:** Offiziell verbietet und begrenzt 6b6t weder TNT noch Redstone noch Entities so, dass eine kleine Perlenkanone blockiert wäre. Technisch ist sie auf 6b6t aber **unzuverlässig**. Die Perle darf nicht lange warten (Stasis-Bug, Issue #7), der Besitzer sollte zwischen Schuss und Landung nicht teleportieren, und lange Schüsse in ungeladenes Gebiet sind riskant (#7, #28, Chunk-Ticket unklar). Der Server läuft auf 26.2 (siehe [6b6t-regeln-limits.md](6b6t-regeln-limits.md)). Dort behält der Spieler nach dem Perlen-Teleport seinen Schwung (3.4).

---

## 6. Was nicht gefunden wurde

- Keine verifizierte, schriftlich dokumentierte **kleine** TNT-Perlenkanone für 1.21+/26.x mit Blockliste in Wiki oder Originalquellen. Der Bauvorschlag in 2.2 ist abgeleitet und nicht getestet.
- Kein Inhalt der Videos geprüft (2.3).
- Keine Messung im Spiel. Alle Reichweiten in 1.6 sind simuliert.
- Augenhöhe der Perle (0,2125) nicht im Bytecode des Builders geprüft (1.4).
- Für 6b6t: nicht geklärt, ob fliegende Perlen Chunks laden (Abschnitt 5). Discord-Regeln nicht gelesen.

---

## Kernzahlen

| Größe | Wert | Quelle |
|---|---|---|
| Stoß pro TNT | `(1 − d/8) · exposure` Blöcke/Tick, also 0,90 bei 0,79 Abstand, 0,75 bei 2 Blöcken | `ServerExplosion.hurtEntities`, [Wiki](https://minecraft.wiki/w/Explosion) |
| Mehrere TNT | addieren sich als Vektoren | Code `Entity.push`, [Wiki](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon) |
| Weite pro TNT | obere Schranke ≈ 89 Blöcke (99 × 0,9). Auf flachem Boden ≈ 60–90 Blöcke pro weiterem TNT (4–16 TNT); das erste TNT bringt nur ≈ 13–21 | 1.6 (simuliert), [Wiki](https://minecraft.wiki/w/Ender_Pearl) |
| Kleine Kanone (≈ 8–16 Spender) | ≈ 400–1.200 Blöcke, je nach Winkel | 1.6 (simuliert) |
| Zündschnur / Stärke | 80 Ticks / 4.0 | `PrimedTnt` |
| Perle | Schwerkraft 0,03, Luftwiderstand 0,99 (Wasser 0,8), keine Geschwindigkeitsgrenze, nicht explosionsimmun | `ThrowableProjectile`, `Entity` |
| 6b6t | keine Regel und kein bekanntes Limit gegen TNT/Redstone. Perlen-Handling durch Folia/Fox gestört | [Rules](https://6b6t.org/en/rules), [Issue #7](https://github.com/6b6t/feedback/issues/7) |
