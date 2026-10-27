# TNT-Duper (MC 26.2/26.3, 6b6t) – Recherche

Stand: 2026-10-27. Ziel: klären, ob klassische TNT-Duper (Vanilla-Redstone, Kolben + Schleim + Korallenfächer/Schiene/Teppich) in Minecraft Java 26.2 und auf 6b6t funktionieren. Außerdem: Wie sieht ein kleiner, billiger Duper aus, und wie hängt man ihn an eine Perlenkanone?
**Nicht im Umfang:** Client-Exploits, Packet-Spoofing, Anti-Cheat-Umgehung, Item-Dupes. Es geht nur um die klassische TNT-Duplikation per Kolben, die Mojang bewusst im Spiel lässt (siehe 1.3). Physik der Perlenkanone: siehe [perlenkanone.md](perlenkanone.md), 6b6t-Grundlagen: siehe [6b6t-regeln-limits.md](6b6t-regeln-limits.md). Beides wird hier nicht wiederholt.

Alle Quellen wurden am **2026-10-27** abgerufen.

| Quelle | Art | Status |
|---|---|---|
| Spiel-Jars 26.2 (`minecraft-merged-201bed67e3-26.2.jar`) und 26.3 (`minecraft-merged-7e9a32a5b8-26.3.jar`), `PistonBaseBlock`, `PistonMovingBlockEntity`, `TntBlock`, `BaseCoralFanBlock`, `BaseCoralWallFanBlock`, `LevelChunk`, gelesen mit `javap -c -p` | Spielcode | Primärquelle |
| Paper-Quellcode, Branch `main` (Commit `4f49049`, 2026-10-26) und `5d4f9bd` (die Paper-Basis von Folia 26.2): `PistonBaseBlock.java.patch`, `PistonMovingBlockEntity.java.patch`, `GlobalConfiguration.java` | Server-Code | Primärquelle |
| [Paper-Doku: Global configuration](https://docs.papermc.io/paper/reference/global-configuration/), [Paper bug fixes](https://docs.papermc.io/paper/misc/paper-bug-fixes/), [Paper PR #3544](https://github.com/PaperMC/Paper/pull/3544) (Spottedleaf, 2020) | Server-Doku | Primärquelle |
| Folia-Quellcode, Branch `ver/26.2.x` (`gradle.properties`, `0001-Region-Threading-Base.patch` für `minecraft-patches` und `paper-patches`) | Server-Code | Primärquelle |
| Mojang-Bugtracker über den Spiegel [mojira.dev](https://mojira.dev) ([MC-279326](https://mojira.dev/MC-279326), [MC-279475](https://mojira.dev/MC-279475), [MC-190643](https://mojira.dev/MC-190643)). bugs.mojang.com selbst ist eine JS-App, bugs-legacy war nicht erreichbar | Bugtracker | Primärquelle (Spiegel) |
| [minecraft.wiki: Tutorial:Block and item duplication](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication) (Abschnitte „Piston-based TNT duplication“ und „…rails and carpets“), [Piston](https://minecraft.wiki/w/Piston), [Dead Coral Fan](https://minecraft.wiki/w/Dead_Coral_Fan), [Coral Fan](https://minecraft.wiki/w/Coral_Fan), [Slime Block](https://minecraft.wiki/w/Slime_Block), [Honey Block](https://minecraft.wiki/w/Honey_Block), [Slimeball](https://minecraft.wiki/w/Slimeball) | Wiki | Primärquelle (Community) |
| [random-j-farmer: TNT dupers](https://random-j-farmer.github.io/random-minecraft/2021/01/03/tnt-dupers.html) (2021) | Sammlung mit Erfindernamen und Videolinks | Sekundär |
| Videos von Cheesysponges, ilmango, Ray Works, Eta740 (Links in 4.3) | Videos | **Nicht angesehen**, nur als Hinweis gelistet |
| [6b6t.org/en/rules](https://6b6t.org/en/rules) (Stand laut [6b6t-regeln-limits.md](6b6t-regeln-limits.md)), [blog.6b6t.org](https://blog.6b6t.org/) (Dupe-Event-Posts), [github.com/6b6t/feedback](https://github.com/6b6t/feedback) (alle 34 Issues durchsucht) | Betreiber | offiziell |

---

## 1. Der Mechanismus und ob er in 26.2 noch da ist

### 1.1 Was im Code passiert (`PistonBaseBlock.moveBlocks`, Bytecode 26.2)

1. `PistonStructureResolver.resolve()` sammelt die Blöcke, die verschoben werden (`toPush`), und die, die dabei zerbrechen (`toDestroy`).
2. **Für jede `toPush`-Position wird der Blockzustand *sofort* gelesen und in eine Liste kopiert** (`Level.getBlockState` → `List.add`, Bytecode-Offset 105–139). Gleichzeitig wandert er in eine Map `deleteAfterMove`.
3. Die `toDestroy`-Blöcke werden gedroppt und mit Flag 18 durch Luft ersetzt (Offset 258–304).
4. Für jede verschobene Position wird am Ziel ein `MOVING_PISTON` gesetzt (Flag 324). Die zugehörige `PistonMovingBlockEntity` bekommt den **Zustand aus der Liste von Schritt 2**, nicht den aktuellen Zustand der Welt (Offset 421–449: `List.get` → `MovingPistonBlock.newMovingBlockEntity`).
5. Danach werden die alten Positionen geleert (Flag 82), und es folgen Form- und Nachbar-Updates (`updateNeighbourShapes`, `affectNeighborsAfterRemoval`, `updateNeighborsAt`, Offset 650–919).

Die Lücke steckt zwischen Schritt 2 und 4: Zündet ein Update in diesem Zeitraum den TNT-Block, schreibt `TntBlock.neighborChanged` bzw. `onPlace` → `hasNeighborSignal` → `prime()` → `removeBlock`. `prime()` erzeugt `PrimedTnt` auf `(x+0,5, y, z+0,5)` mit dem normalen Zufallssprung (Bytecode `TntBlock.prime`, gesperrt nur durch die Spielregel `TNT_EXPLODES`). Der Kolben setzt den TNT-Block aber trotzdem am Ziel ab, weil er die alte Kopie benutzt. Ergebnis: ein gezündetes TNT **und** der TNT-Block, der weiterwandert.

So beschreibt es auch Spottedleaf im Paper-Patch von 2020 ([PR #3544](https://github.com/PaperMC/Paper/pull/3544), Patch „Fix piston physics inconsistency - MC-188840“):
> „Pistons invoke physics when they move blocks. The physics can cause tnt blocks to ignite. However, pistons (when storing the blocks they "moved") don't actually go back to the world state sometimes to check if something like that happened. As a result they end up moving the tnt like it was never ignited.“ … „dead coral is removed by a physics update when being moved while it is attached to slimeblocks.“

Das Wiki sagt es kürzer: „A TNT block is BUD-powered, such that when the contraption is pushed by a piston, the TNT is updated and ignites into a primed TNT entity. Then, the piston moves the TNT from the list of TNT block left.“ ([Wiki: Duplication](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication)). Zum Korallenfächer: „in some situations the coral will both break and get moved by the piston, emitting block updates at its original position. … This mechanic is frequently used in TNT dupers.“ ([Wiki: Piston](https://minecraft.wiki/w/Piston)).

**Rolle der Bauteile:** Der TNT-Block ist bereits „BUD-powered“, hat also Strom (z. B. von einer Detektorschiene mit Lore), wurde aber noch nicht aktualisiert. Der **tote Korallenfächer** (oder Teppich/Schiene) zerbricht während der Bewegung und schickt das Update, das das TNT zündet. Teppich- und Schienen-Dupes nutzen denselben Fehler (Paper: „it's the same bug for those“, [Paper bug fixes](https://docs.papermc.io/paper/misc/paper-bug-fixes/)).

### 1.2 Stand in 26.2 und 26.3

- **26.2:** Die Kopie-Liste aus 1.1 ist im Bytecode vorhanden. Der Mechanismus ist also da.
- **26.3:** Bytecode-Diff von `PistonBaseBlock`, `PistonMovingBlockEntity`, `TntBlock`, `BaseCoralFanBlock` und `BaseCoralWallFanBlock` (normalisiert): Es gibt nur Umbenennungen (`PushReaction.NORMAL` → `PUSH_PULL`, `IGNORE` → `IGNORE_ENTITY`), die entfernte `codec()`-Methode, `TntBlock.prime` mit zusätzlichem `ItemStack`-Parameter und `setBlockAndUpdate` statt `setBlock(…, 3)`. **An `moveBlocks` hat sich nichts geändert.**
- **Regression 2025 und Fix:** In Snapshot 25w02a (Weg zu 1.21.5) hörten TNT- und Teppich-Duper auf zu funktionieren, weil `LevelChunk.setBlockState` `affectNeighborsAfterRemoval` nur noch mit `UPDATE_NEIGHBORS` aufrief ([MC-279475](https://mojira.dev/MC-279475), als Duplikat von [MC-279326](https://mojira.dev/MC-279326) geschlossen). MC-279326 („Lit observers no longer send block updates when moved by a piston“) wurde von **gnembon** (Mojang) bearbeitet und ist in **25w02a/25w03a als „Fixed“** markiert. Im 26.2-Bytecode von `LevelChunk.setBlockState` prüft eine Bedingung jetzt Flag 64 (`UPDATE_MOVE_BY_PISTON`) vor `affectNeighborsAfterRemoval`. Das passt zu dem Kommentar von VelizarBG im Ticket. Ein Mod-Kommentar in MC-279475 sagt allerdings auch: „duplication is not a feature. If only duplicators breaks, then it's not a bug.“
- **Changelogs 1.21.x–26.x:** In den Wiki-Seiten zu Piston, TNT und Dead Coral Fan steht keine Änderung an der Duplikation (Wiki-Rohtext durchsucht). *Die einzelnen Changelogs habe ich nicht alle gelesen.*
- [MC-190643](https://mojira.dev/MC-190643) (L-förmige Schleimblöcke mit Schiene BUD-powern den Kolben, Stand „Reopened“) ist ein Nebeneffekt bei Duper-Bauten, kein Fix.

### 1.3 Absicht von Mojang

- Wiki: „Mojang tries to remove the ability to duplicate in Survival during almost every update, except for primed TNT duplication, which has been left in on purpose until a suitable replacement exists.“ Zitat von Mikael Hedberg (Mojang): „TNT dupers fill a slot in the game that the intended game mechanics still cannot.“ ([Wiki](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication), mit Link auf den [Reddit-Kommentar](https://www.reddit.com/r/Minecraft/comments/fkt9jf/anchor_yourself_to_the_nether_snapshot_20w12a_is/fkv2yyv/); *den Reddit-Kommentar selbst habe ich nicht geöffnet*).
- Das Wiki führt die TNT-Duplikation als „1.14+“, Teppich und Schienen als „useful in 1.19“. Die Seite „Item Duplication Methods“ im Discontinued-Features-Wiki nennt für Teppich/Schienen „From 1.12.2 to 26.4 Snapshot 5 (present)“ ([mcdf.wiki.gg](https://mcdf.wiki.gg/wiki/Java_Edition:Item_Duplication_Methods)).

**Ergebnis 1:** In Vanilla 26.2 (und 26.3) funktioniert die TNT-Duplikation per Kolben. Belegt durch Bytecode und den Mojang-Fix von 2025. *Im Spiel getestet habe ich es nicht.*

---

## 2. Paper und Folia

### 2.1 Paper: Schlüssel und Standardwert

- Datei `config/paper-global.yml`, Abschnitt `unsupported-settings`, Schlüssel **`allow-piston-duplication`**, Standard **`false`** ([Doku](https://docs.papermc.io/paper/reference/global-configuration/)): „Whether to allow duplication of TNT, carpets, and rails. This does not control sand duplication“.
- Im Code: `GlobalConfiguration.UnsupportedSettings.allowPistonDuplication = false`, Kommentar: „This setting controls if player should be able to use TNT duplication, but this also allows duplicating carpet, rails and potentially other items“ (`GlobalConfiguration.java`, Zeile 177–178, `main` und `5d4f9bd`).
- **So wirkt der Patch** (`PistonBaseBlock.java.patch`, „fix a variety of piston desync dupes“): Steht der Schalter auf `false`, liest Paper den Zustand an der alten Position **direkt vor dem Erzeugen** der `PistonMovingBlockEntity` neu, schreibt ihn in `deleteAfterMove` und setzt die alte Position sofort auf Luft. Wurde das TNT vorher gezündet, ist der neue Zustand Luft, und es wandert nichts weiter. Dazu kommt `UPDATE_CLIENTS` in `PistonMovingBlockEntity`. Steht der Schalter auf `true`, wird die alte Vanilla-Kopie benutzt.
- Begründung von Paper: „Because TNT duping is considered a form of automated mining and not a resource dupe, we have provided an option to restore it. This, undesirably, also re-enables carpet and rail duping“ ([Paper bug fixes](https://docs.papermc.io/paper/misc/paper-bug-fixes/)).
- Verwandte Schlüssel im selben Abschnitt (alle Standard `false`, außer wo anders angegeben): `allow-headless-pistons`, `allow-permanent-block-break-exploits`, `allow-unsafe-end-portal-teleportation` (Sand-Dupe), `skip-tripwire-hook-placement-validation`; `perform-username-validation` und `update-equipment-on-player-actions` stehen auf `true` ([Doku](https://docs.papermc.io/paper/reference/global-configuration/)).
- **Folge auf einem Paper-Server mit Standard-Config:** Ein Duper „frisst“ das TNT. Es zündet einmal, der Block ist danach weg. Laut Wiki „Many TNT duplicators still work, but unless properly constructed will blow themselves up“ (gilt für Vanilla); auf Paper gibt es gar kein zweites TNT.

### 2.2 Folia

- Folia ist eine Patch-Schicht über Paper. `ver/26.2.x` baut auf `paperRef=5d4f9bd0…` (Paper-Commit vom 2026-08-25). Dort ist der Paper-Patch mit `allowPistonDuplication` enthalten (geprüft).
- Folias eigene Patches für `PistonBaseBlock` und `PistonMovingBlockEntity` ändern nur die Zeitbasis (`level.getGameTime()` → `level.getRedstoneGameTime()`, Region-Threading) und Feldinitialisierungen. Den Duplikations-Fix oder den Config-Schlüssel ändern sie **nicht** (Patch `0001-Region-Threading-Base.patch` durchsucht; die `paper-patches` erwähnen Kolben gar nicht).
- **Ergebnis 2:** Folia übernimmt `unsupported-settings.allow-piston-duplication: false` unverändert. Folia selbst dupliziert also standardmäßig **kein** TNT.

---

## 3. 6b6t

**Belegt:**
- 6b6t läuft auf **Fox**, einem eigenen Folia-Downstream, Version 26.2 ([6b6t-regeln-limits.md](6b6t-regeln-limits.md), Abschnitte 2 und 4; [Issue #4](https://github.com/6b6t/feedback/issues/4), [#10](https://github.com/6b6t/feedback/issues/10)). Ohne Änderung durch Fox gilt also der Paper-Standard `allow-piston-duplication: false`.
- Fox ändert bewusst Kolben-nahe Dinge: Laut [Issue #16](https://github.com/6b6t/feedback/issues/16) gibt es in Fox ein eigenes „bedrock breaking“, das nur „solid full-cube blocks“ akzeptiert (End-Portalrahmen ausgenommen). Der Betreiber stellt also mindestens einen Teil der `unsupported-settings`-Exploits gezielt ein. *Welche Kolben-Einstellung Fox dabei nutzt, steht nirgends.*
- Regeln: „Duping is part of anarchy gameplay, but exploits may be patched.“ ([Rules](https://6b6t.org/en/rules), zitiert in [6b6t-regeln-limits.md](6b6t-regeln-limits.md)). Ein Verbot gibt es nicht. TNT, Redstone und Kolben werden in den Regeln nicht erwähnt ([perlenkanone.md](perlenkanone.md), Abschnitt 5).
- Im August 2026 gab es ein **Dupe-Event**: „On August 14th at 20:00 CEST, a powerful dupe will be enabled on 6b6t for 72 hours“, verlängert auf 9,5 Tage ([Blog-Ankündigung](https://blog.6b6t.org/6b6t-dupe-event-august-14-2026/), [Blog-Bericht](https://blog.6b6t.org/6b6t-dupe-event-live-1155-players/): „we enabled many powerful dupes across 6b6t“). Daraus folgt: Dupes sind außerhalb solcher Events normalerweise abgeschaltet. Welche Dupes das waren, sagen die Posts nicht.
- Im Feedback-Tracker (34 Issues, Stand 2026-10-27) gibt es **keinen Treffer** zu „tnt“, „piston“ (nur ein Treffer ohne Bezug, #14), „duplication“, „allow-piston“, „paper-global“ oder „redstone“.

**Unbelegt / Gerücht:** Ob TNT-Duper auf 6b6t funktionieren, ist **nicht belegt**. Spielerberichte habe ich keine gefunden; eine Websuche lieferte nur Spam-Seiten ohne 6b6t-Bezug. Discord wurde nicht gelesen. Die Server-Config von 6b6t ist nicht öffentlich.

**Einschätzung (hergeleitet):** Eher **nein**. Der Unterbau (Paper/Folia) blockiert es standardmäßig, und der Betreiber schaltet Dupes nur zu Events frei. Sicher wissen kann man es nur per Test (Abschnitt 4.4).

---

## 4. Kleiner, billiger Duper für 26.x

### 4.1 Die Wiki-Bauweise (Cheesysponges-Typ, Push-Duper)

Laut [Wiki](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication), Abschnitt „Piston-based TNT duplication“: „uses a minecart on a detector rail to power the slime block it is placed on, and moves a TNT block next to a coral fan underneath“. Ausführliches Tutorial laut Wiki: Video von **Cheesysponges** ([3Izrj2m6Li4](https://youtu.be/3Izrj2m6Li4), *nicht angesehen*).

Schema aus dem Wiki (Seitenansicht, die Bewegung geht nach rechts; „vi“ = toter Korallenfächer, so im Wiki markiert):

```
Scheibe 1                         Scheibe 2 (seitlich daneben, egal welche Seite)
.   .    .     DR   SL            .   .   .
.   .    WL    SL   SL            .   .   SL
.   KF   TNT   .    .             .   SL  SL
DR = Detektorschiene (mit Lore), SL = Schleimblock, WL = Block "wl" im Wiki-Schema,
KF = toter Korallenfächer, TNT = TNT-Block
```

- Ablauf laut Wiki: „To prime the mechanism, the upper slime blocks must first be moved forward, pushing the TNT under the rail. After this, moving the whole assemblage forward with a piston will duplicate the TNT, spawning a lit TNT entity directly below the TNT block.“
- *Das Kürzel „wl“ ist im Wiki-Schema nicht erklärt. Welcher Block gemeint ist, habe ich nicht geklärt.* Wer das baut, sollte das Video oder eine Schematic als Vorlage nehmen.

### 4.2 Blockliste und Kosten (eine Duper-Einheit, hergeleitet aus 4.1)

| Teil | Anzahl | Rohstoffe |
|---|---|---|
| Schleimblock | 6 (3 in Scheibe 1, 3 in Scheibe 2) | 54 Schleimbälle |
| Detektorschiene | 1 | Rezept ergibt 6 Stück aus 6 Eisen + Steindruckplatte + Redstone |
| Lore | 1 | 5 Eisen |
| TNT-Block | 1 (wird nicht verbraucht) | 5 Schwarzpulver + 4 Sand |
| Toter Korallenfächer | 1 | lebenden Fächer mit Behutsamkeit abbauen, an Land setzen, er stirbt („after a few seconds“ in Java, [Wiki](https://minecraft.wiki/w/Dead_Coral_Fan)) |
| „wl“-Block | 1 | unklar (s. o.) |
| Antrieb | 1 klebriger Kolben + 2 Observer (Taktgeber) oder Kolben + Knopf | Kolben: 3 Bretter, 4 Bruchstein, 1 Eisen, 1 Redstone, 1 Schleimball; Observer: 6 Bruchstein, 2 Redstone, 1 Netherquarz |

**Summe pro Einheit:** etwa **55 Schleimbälle, ~7 Eisen, 5 Redstone, 1 Quarz, 1 TNT**, plus Kleinkram. Teuer ist nur der Schleim. Honigblöcke (4 Honigflaschen) können Schleim dort ersetzen, wo Blöcke nicht aneinanderkleben sollen; Schleim und Honig kleben nie aneinander ([Wiki: Honey Block](https://minecraft.wiki/w/Honey_Block)). ilmango hat einen Honig-Duper (3 Blöcke hoch) gebaut ([random-j-farmer](https://random-j-farmer.github.io/random-minecraft/2021/01/03/tnt-dupers.html), Video [zc3Vecx2xoU](https://youtu.be/zc3Vecx2xoU?t=120), *nicht angesehen*).

### 4.3 Weitere bekannte Duper (Sekundärquelle, nicht geprüft)

Laut [random-j-farmer](https://random-j-farmer.github.io/random-minecraft/2021/01/03/tnt-dupers.html) (Stand 2021, Korallenfächer-Duper „since 1.13.1“): Mytteri-Duper (Push, bekannt durch ilmango, [8KAPO4NScQ8](https://youtu.be/8KAPO4NScQ8?t=73)), Kades-Duper (Pull, „exact“ mit 2 Wänden + Zauntor ausgerichtet, gut für stationäre Maschinen), Mytteri 2-Way, Ray Works Slim Push Duper (1 Block breit, [CJAbnB4p52g](https://youtu.be/CJAbnB4p52g?t=286)), Eta740 „Standard TNT Dupers“ ([xs2XVyZ0tGY](https://youtu.be/xs2XVyZ0tGY?t=130)), ilmango 5-Way ([TyT2TdkDs0Q](https://www.youtube.com/watch?v=TyT2TdkDs0Q)). Blocklisten nennt die Seite nicht. Ob sie nach der Änderung in 25w02a/25w03a (1.2) unverändert laufen, ist **unbekannt**.

### 4.4 Test auf 6b6t, bevor man viel baut

Ziel: mit wenig Material prüfen, ob `allow-piston-duplication` (oder etwas Gleichwertiges in Fox) an ist. Laut Paper-Code deckt **ein** Schalter TNT, Teppich und Schienen ab (2.1). Ein Teppich-Duper reicht also als Test, ohne Explosion und ohne TNT-Verlust.

**Test A: Teppich-Duper (billigster Test, kein TNT)**, Bauweise laut [Wiki](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication):

```
Untere Ebene:  O B        Obere Ebene:  . .
               S P                      S C
O = Observer, B = glatter Stein, S = Schleimblock, P = klebriger Kolben, C = Teppich
```

Kosten: 2 Schleimblöcke (18 Schleimbälle), 1 klebriger Kolben, 1 Observer, ein paar Teppiche. Den Observer auslösen (z. B. einen Block vor seinem Gesicht setzen) und zusehen, ob Teppiche als Items herausfallen, **während** der Teppich auf dem Schleim bleibt. Vorlage laut Wiki: Video [ezM2Hs_-hbE](https://www.youtube.com/watch?v=ezM2Hs_-hbE) (*nicht angesehen*). Die genaue Ausrichtung (welche Seite der Observer „sieht“) steht im Wiki-Schema nicht. Deshalb vorher in Singleplayer 26.2 nachbauen, bis es dort läuft.

**Test B: eine TNT-Einheit** (nur wenn A klappt), weit weg von der Basis. Erwartung:
- **Duping aktiv:** Pro Kolbenhub fällt ein gezündetes TNT, der TNT-Block bleibt.
- **Duping aus (Paper-Standard):** Das TNT zündet einmal, der Block ist danach weg (2.1).

Beide Tests sind reine Vanilla-Bauten. Das Ergebnis ist nur eine Momentaufnahme: „exploits may be patched“ ([Rules](https://6b6t.org/en/rules)).

### 4.5 Duper + Perlenkanone

- Ein Duper liefert pro Hub **ein** gezündetes TNT auf `(Block-X + 0,5, Block-Y, Block-Z + 0,5)` unter dem TNT-Block, mit demselben Zufallssprung (0,2 nach oben, 0,02 seitlich) wie beim Spender (Bytecode `TntBlock.prime`, [perlenkanone.md](perlenkanone.md) 1.3). Für die Zielgenauigkeit ist ein Duper also nicht besser als ein Spender. Der Vorteil: **Das TNT kostet nichts** außer dem einen Block.
- Weil sich die Stöße pro TNT addieren und alle TNT im selben Tick explodieren sollen (Zündschnur 80 Ticks, [perlenkanone.md](perlenkanone.md) 1.3, 1.5), braucht man für **N TNT pro Schuss N Duper-Einheiten, die im selben Tick ausgelöst werden.** Ein Duper, der mehrmals hintereinander auslöst, liefert TNT, das in verschiedenen Ticks explodiert. *Ob man das sinnvoll nutzen kann, habe ich nicht untersucht.*
- Ausrichtung wie in [perlenkanone.md](perlenkanone.md) 2.2: Die Duper sitzen auf der Gegenseite des Ziels und so, dass das gefallene TNT in der Wasser- oder Lavakammer neben der Perle landet (Abstand ≥ 0,79 Blöcke, keine festen Blöcke dazwischen). Die großen 360°-Kanonen bringen das TNT mit „alignern“ auf exakt dieselbe Position ([Wiki: 360 cannon](https://minecraft.wiki/w/Tutorial:360_degree_ender_pearl_cannon)). Das ist **nicht klein**.
- TNT-Bedarf pro Schuss (eigenes Skript, Tick-Reihenfolge wie in [perlenkanone.md](perlenkanone.md) 1.6, optimaler Winkel, flacher Boden, alle TNT voll wirksam):

  | Weite | TNT bei 0,9 Bl./Tick pro TNT (0,79 Abstand) | TNT bei 0,75 Bl./Tick (2 Blöcke Abstand) |
  |---|---|---|
  | 1.000 | 14 | 17 |
  | 5.000 | 58 | 70 |
  | 10.000 | 114 | 137 |

  *Hergeleitet, nicht im Spiel gemessen.* In der Praxis braucht man mehr, weil sich das TNT gegenseitig wegschiebt (perlenkanone.md 1.5).

---

## 5. Material farmen (kurz)

- **Schleim:** Schleime spawnen in Schleim-Chunks unter Y 40 und in Sümpfen nachts. Baby-Pandas niesen Schleimbälle (1/700), wandernde Händler verkaufen 1 Schleimball für 4 Smaragde ([Wiki: Slimeball](https://minecraft.wiki/w/Slimeball)). 1 Schleimblock = 9 Schleimbälle.
- **Honig:** Honigflaschen aus Bienennestern mit Glasflaschen (Lagerfeuer darunter, damit die Bienen ruhig bleiben). 4 Flaschen = 1 Honigblock ([Wiki: Honey Block](https://minecraft.wiki/w/Honey_Block)).
- **Korallenfächer:** Nur mit Behutsamkeit abbaubar. Wachsen per Knochenmehl in warmen Ozeanen nach („renewable“ seit 1.13.1). Tote Fächer entstehen, wenn man lebende an Land setzt. Tote Wandfächer gibt es in Java nicht als Item ([Wiki: Coral Fan](https://minecraft.wiki/w/Coral_Fan), [Dead Coral Fan](https://minecraft.wiki/w/Dead_Coral_Fan)). Ein Fächer reicht pro Duper.
- **Observer:** 6 Bruchstein, 2 Redstone, 1 Netherquarz. Quarz gibt es im Nether überall.
- **TNT:** nur 1 pro Duper. Wenn das Duping aus ist, braucht man pro Schuss die volle Zahl aus 4.5 (5 Schwarzpulver + 4 Sand pro TNT).

*Die genauen Spawnregeln für Schleim habe ich für 26.2 nicht im Code nachgeprüft.*

---

## 6. Vergleich: TNT-Duper-Kanone vs. Bean Blaster

Zahlen für den Bean Blaster aus [bean-blaster.md](bean-blaster.md), Abschnitte 6 und 7. Zahlen für TNT aus 4.5 (Spalte 0,9–0,75 Bl./Tick).

| Weite | Bean Blaster (pro Schuss) | TNT-Kanone mit Spendern (pro Schuss) | TNT-Kanone mit Dupern |
|---|---|---|---|
| 1.000 | 29–39 Windkugeln = 8–10 Breeze-Ruten | 14–17 TNT = 70–85 Schwarzpulver + 56–68 Sand | 0 pro Schuss; einmalig 14–17 Duper ≈ 770–940 Schleimbälle |
| 5.000 | 149–197 Windkugeln = 38–50 Ruten | 58–70 TNT = 290–350 Schwarzpulver | einmalig 58–70 Duper ≈ 3.200–3.900 Schleimbälle |
| 10.000 | 299–454 Windkugeln = 75–114 Ruten | 114–137 TNT = 570–685 Schwarzpulver | einmalig 114–137 Duper ≈ 6.300–7.600 Schleimbälle |

- **Bauaufwand:** Der Bean Blaster nutzt eine feste Maschine, die Weite stellt man über die Anzahl Windkugeln ein. Eine Duper-Kanone braucht für jede Weitenstufe eine eigene Duper-Einheit. Klein bleibt sie nur bis etwa 1.000 Blöcke.
- **Genauigkeit:** Bean Blaster laut Erfinder ≈ 80–150 Blöcke Abweichung (bean-blaster.md). Eine kleine TNT-Kanone ohne Ausrichtung streut stärker (Zufallssprung, TNT schiebt TNT; perlenkanone.md 1.5). Ein Zahlenwert ist nicht belegt.
- **Zuverlässigkeit auf 6b6t:**
  - Bean Blaster: erlaubt, reine Vanilla-Mechanik, **funktioniert technisch auch unter Paper/Folia**. Risiken sind nur die Perlen-Bugs (Stasis/Worker, [Issue #7](https://github.com/6b6t/feedback/issues/7); Kick beim Laden, [#28](https://github.com/6b6t/feedback/issues/28)).
  - TNT-Duper-Kanone: dieselben Perlen-Risiken, **und** das Duping ist auf Paper/Folia standardmäßig aus (Abschnitt 2). Ohne bestandenen Test (4.4) ist sie auf 6b6t nicht planbar. Mit Spendern statt Dupern funktioniert sie, kostet dann aber viel Schwarzpulver pro Schuss.
- **Fazit:** Auf 6b6t ist der Bean Blaster die verlässlichere Wahl. Eine Duper-Kanone lohnt sich nur, wenn der Test in 4.4 positiv ausfällt **und** man oft auf ähnliche Weiten schießt.

---

## 7. Was nicht gefunden wurde

- Kein Beleg, ob 6b6t/Fox `allow-piston-duplication` an- oder ausgeschaltet hat. Keine Spielerberichte, Discord nicht gelesen.
- Die Status-Seite von [MC-188840](https://bugs.mojang.com/browse/MC-188840) (im Paper-Patch zitiert) ist privat oder nicht auffindbar, ebenso MC-100579. Den Stand im offiziellen Tracker habe ich nur über den Spiegel mojira.dev gesehen.
- Das Kürzel „wl“ im Wiki-Schema (4.1) ist nicht geklärt. Die Ausrichtung im Teppich-Schema (4.4) ist nicht eindeutig. Keine Videos angesehen.
- Nichts im Spiel getestet: weder Duper noch Kanone, weder in 26.2 noch auf 6b6t.
- Nicht jede Changelog-Seite 1.21.x–26.x einzeln gelesen. Der Beleg für 26.2/26.3 ist der Bytecode.
- Keine kleine Duper-Perlenkanone mit Bauplan in Primärquellen gefunden. 4.5 ist abgeleitet.

---

## Kernzahlen

| Größe | Wert | Quelle |
|---|---|---|
| Mechanismus | Kolben kopiert Blockzustände vor den Updates; gezündetes TNT wird trotzdem verschoben | `PistonBaseBlock.moveBlocks` (26.2), [Paper PR #3544](https://github.com/PaperMC/Paper/pull/3544) |
| Vanilla 26.2 / 26.3 | funktioniert (Code unverändert; Regression 25w02a von Mojang behoben) | Bytecode, [MC-279326](https://mojira.dev/MC-279326) |
| Paper / Folia | `unsupported-settings.allow-piston-duplication`, Standard `false`; Folia übernimmt das | [Paper-Doku](https://docs.papermc.io/paper/reference/global-configuration/), Paper/Folia-Code |
| 6b6t | Fox (Folia-Fork). Duping funktioniert dort **unbelegt**, eher nicht; Dupes nur bei Events | [Blog](https://blog.6b6t.org/6b6t-dupe-event-august-14-2026/), [Rules](https://6b6t.org/en/rules) |
| Duper-Einheit | ≈ 6 Schleimblöcke, 1 Detektorschiene + Lore, 1 toter Korallenfächer, 1 TNT, klebriger Kolben + 2 Observer ≈ 55 Schleimbälle | 4.2 (aus [Wiki](https://minecraft.wiki/w/Tutorial:Block_and_item_duplication)) |
| Test | Teppich-Duper: 2 Schleimblöcke, klebriger Kolben, Observer, Teppich | 4.4 |
| TNT pro Schuss | 1.000 / 5.000 / 10.000 Blöcke ≈ 14–17 / 58–70 / 114–137 TNT | 4.5 (simuliert) |
