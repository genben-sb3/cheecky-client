# Schnell an Materialien kommen (Anarchy-Server) – Recherche

Stand: 2026-10-23. Ziel: Für den eigenen Fabric-Client „Cheecky“ (MC 26.3, Loader 0.19.5, Fabric API 0.164.0+26.3, Java 25, Mojang-Namen) herausfinden, was man **installiert** (Baritone) und was man **selbst baut** (Nuker, Excavator/Tunneller, AutoSteal, AutoTool). Einsatz auf Anarchy-Servern, deren Regeln Hacks erlauben.
**Nicht im Umfang:** Anti-Cheat-Umgehung (z. B. Meteors `grim-bypass` in SpeedMine oder Wursts NukerLegit) und Anleitungen für Dupes. Abschnitt 3 nennt nur den Faktenstand.

Alle Quellcode-Links sind Permalinks auf die gelesenen Commits:

| Repo | Commit (Datum) |
|---|---|
| cabaletta/baritone (Tag `v1.20.0`) | [`25111da`](https://github.com/cabaletta/baritone/tree/25111daedf1d59e6a8dfb5a3e61885cdb8d953df) (2026-09-22) |
| MeteorDevelopment/baritone (Branch `26.3`, Meteors Fork) | [`1cdb177`](https://github.com/MeteorDevelopment/baritone/tree/1cdb177f196fb49d11b59367a526b237e34dda97) (2026-10-17) |
| MeteorDevelopment/meteor-client | [`1532c9e`](https://github.com/MeteorDevelopment/meteor-client/tree/1532c9e2191260d4dc80be1889355e2dab442c15) (2026-10-22) |
| Wurst-Imperium/Wurst7 | [`c040d7a`](https://github.com/Wurst-Imperium/Wurst7/tree/c040d7accda6d270707ac257f906d7387a495af3) (2026-10-21) |
| Minecraft 26.3 (dekompiliert, lokal) | Methodennamen unten wurden darin geprüft |

---

## 1. Baritone

### 1.1 Gibt es ein Release für 26.3?

**Ja.** [Release `v1.20.0`](https://github.com/cabaletta/baritone/releases/tag/v1.20.0) vom 2026-09-23, Release-Text: „For Minecraft 26.3. Forge is supported. Fabric is supported. NeoForge is supported.“ Im Tag steht `minecraft_version=26.3`, `fabric_version=0.19.5`, `java_version=25` ([gradle.properties](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/gradle.properties)).

Fabric-Dateien im Release (Abfrage der GitHub-Releases-API am 2026-10-23):

| Datei | Zweck laut [SETUP.md](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/SETUP.md) (Z. 28–41) |
|---|---|
| `baritone-standalone-fabric-1.20.0.jar` | „Everything is obfuscated. Other mods cannot use Baritone, but you get a bit of extra performance.“ Empfohlen, wenn kein anderer Mod Baritone nutzt. |
| `baritone-api-fabric-1.20.0.jar` | „If another one of your other mods has a Baritone integration, you want `baritone-api-*-VERSION.jar`.“ Nur die API-Pakete bleiben lesbar. |
| `baritone-unoptimized-fabric-1.20.0.jar` | Nichts verschleiert, für Crash-Reports. |

- **Offizieller Vertrieb:** nur über GitHub-Releases. SETUP.md verlinkt ausschließlich die [Releases-Seite](https://github.com/cabaletta/baritone/releases), Releases sind von leijurv GPG-signiert und sollen gegen `checksums.txt` geprüft werden ([SETUP.md Z. 8–20](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/SETUP.md)). Auf Modrinth gibt es kein Baritone-Projekt (Modrinth-Suche nach „baritone“ am 2026-10-23 findet nur Anti-Baritone-Plugins). Auf CurseForge auftauchende Einträge werden vom Repo nirgends verlinkt, gelten also als **inoffiziell**.
- **Abhängigkeiten:** Laut [fabric.mod.json](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/fabric/src/main/resources/fabric.mod.json) nur `fabricloader >=0.19.5` und `minecraft ~26.3`. **Fabric API wird nicht benötigt**, schadet aber nicht. Mod-ID: `baritone`.
- **CurseForge-Custom-Profil:** SETUP.md: „The Forge, NeoForge and Fabric releases can simply be added as a Forge/Neoforge/Fabric mods.“ In einem CurseForge-Profil mit MC 26.3 und Fabric Loader ≥ 0.19.5 legt man das JAR also in den `mods`-Ordner des Profils (in der App über „Open Folder“). Diese Menüführung ist nur durch eine Drittanbieter-Anleitung belegt ([Shockbyte-KB](https://shockbyte.com/billing/knowledgebase/659/How-To-Add-Mods-To-Your-CurseForge-Profiles-and-MultiMC-Instances.html)). Dass Fabric das JAR lädt, folgt aus der fabric.mod.json. Nicht selbst getestet.

### 1.2 Integriert Meteor Baritone noch?

**Ja, aber optional und nicht mitgeliefert.** Die [Meteor-FAQ](https://meteorclient.com/faq) sagt: „Baritone is no longer included with Meteor.“ Ohne Baritone sind die Funktionen, die es brauchen, deaktiviert.

So ist das umgesetzt:
- Kompiliert wird nur gegen Meteors eigenen Fork: `compileOnly(libs.baritone)` mit `meteordevelopment:baritone:26.1-SNAPSHOT` ([build.gradle.kts Z. 88](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/build.gradle.kts#L88), [libs.versions.toml Z. 15–16](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/gradle/libs.versions.toml#L15-L16)). In `fabric.mod.json` steht kein `depends` auf Baritone ([Datei](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/resources/fabric.mod.json)).
- Zur Laufzeit prüft Meteor per `Class.forName("baritone.api.BaritoneAPI")`, ob Baritone da ist, und schaltet dann `BaritonePathManager` ein ([PathManagers.java Z. 31–36](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/pathing/PathManagers.java#L31-L36)). Nur dann werden die Module **Excavator** und **InfinityMiner** registriert ([Modules.java Z. 573–576](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/Modules.java#L573-L576)).
- Das Baritone-Mixin `ComeCommandMixin` wird angewendet, wenn die Mod-ID `baritone` **oder** `baritone-meteor` geladen ist ([MixinPlugin.java Z. 38](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/MixinPlugin.java#L38)). Es zielt auf `baritone.command.defaults.ComeCommand`, und die Config ist `"required": true` ([meteor-client-baritone.mixins.json](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/resources/meteor-client-baritone.mixins.json)).
- **Welches JAR zu Meteor passt:** Meteors Fork hat die Mod-ID `baritone-meteor` ([fabric.mod.json im Fork](https://github.com/MeteorDevelopment/baritone/blob/1cdb177f196fb49d11b59367a526b237e34dda97/fabric/src/main/resources/fabric.mod.json)). Er liegt als `26.3-SNAPSHOT` auf [maven.meteordev.org](https://maven.meteordev.org/snapshots/meteordevelopment/baritone/maven-metadata.xml). Das JAR (`baritone-26.3-20261017.173132-1.jar`) enthält `ComeCommand` unverschleiert. Im offiziellen `baritone-api-fabric-1.20.0.jar` fehlt `baritone/command/defaults/ComeCommand.class` dagegen, weil nur `baritone.api.**` von ProGuard verschont wird ([proguard.pro Z. 26](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/scripts/proguard.pro#L26)). Beide JARs habe ich am 2026-10-23 heruntergeladen und den Inhalt aufgelistet. **Folgerung (nicht getestet):** Meteor zusammen mit dem offiziellen API-JAR dürfte beim Anwenden des Pflicht-Mixins scheitern. Mit dem Standalone-JAR ist laut SETUP.md alles verschleiert, auch die API. Meteor fände dann `baritone.api.BaritoneAPI` nicht, und das Mixin-Problem bliebe wegen der Mod-ID `baritone` trotzdem bestehen. Für Meteor also den Fork nehmen.
- Für **Cheecky** gilt: Wenn Cheecky selbst Baritone-Befehle über die API aufrufen soll, braucht es zur Laufzeit die API-Variante (`baritone-api-fabric-1.20.0.jar`) und im Build `compileOnly` gegen dieselbe API. Wenn Baritone nur per Chat (`#…`) bedient wird, reicht `baritone-standalone-fabric-1.20.0.jar`, und Cheecky braucht keinen Code dafür.

### 1.3 Wichtige Befehle für Materialbeschaffung (aus [USAGE.md](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md))

Das Präfix ist standardmäßig `#` (Z. 5). Ohne Präfix getippte Tippfehler landen sonst im öffentlichen Chat (Z. 7).

| Befehl | Wirkung | Zeile |
|---|---|---|
| `#mine diamond_ore iron_ore`, `#mine 64 diamond_ore` | Erze abbauen, optional mit Menge | [Z. 44](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L44) |
| `#goto x y z`, `#goto x z`, `#goto <block>` | Zu Koordinate oder nächstem Block laufen | [Z. 39](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L39), [Z. 43](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L43) |
| `#tunnel`, `#tunnel 3 2 100` | 1×2-Tunnel bzw. Bereich H×B×T freiräumen | [Z. 50](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L50) |
| `#click`, dann `#help sel` | Bereich auswählen und z. B. freiräumen („cleararea“) | [Z. 17](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L17), [Z. 45](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L45) |
| `#farm [range]` | Pflanzen ernten, nachpflanzen, düngen | [Z. 51](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L51) |
| `#explore [x z]` | Neue Chunks erkunden | [Z. 53](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L53) |
| `#cancel` / `#stop` / `#forcecancel` | Alles abbrechen | [Z. 42](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L42) |

Nützliche Einstellungen (Name in den Chat tippen, um sie umzuschalten, Z. 33): `legitMine` (nur sichtbare Erze), `mineScanDroppedItems`, `allowBreak`, `backfill`, `blocksToAvoidBreaking`, `acceptableThrowawayItems` ([Z. 70–90](https://github.com/cabaletta/baritone/blob/25111daedf1d59e6a8dfb5a3e61885cdb8d953df/USAGE.md?plain=1#L70-L90)).

---

## 2. Wie Meteor und Wurst die Sammel-Module bauen

### 2.1 Die Minecraft-API dahinter (geprüft im dekompilierten 26.3)

| Klasse / Methode | Sichtbarkeit | Zweck |
|---|---|---|
| `MultiPlayerGameMode.startDestroyBlock(BlockPos, Direction)` | public, `boolean` | Abbau beginnen (sendet `START_DESTROY_BLOCK`) |
| `MultiPlayerGameMode.continueDestroyBlock(BlockPos, Direction)` | public, `boolean` | Fortschritt pro Tick, ruft zuerst `ensureHasSentCarriedItem()` auf |
| `MultiPlayerGameMode.stopDestroyBlock()` | public | Abbau abbrechen |
| `MultiPlayerGameMode.isDestroying()` | public | Läuft gerade ein Abbau? |
| `MultiPlayerGameMode.startPrediction(ClientLevel, PredictiveAction)` | **private** | Paket mit Sequenznummer senden. Meteor macht es über Class-Tweaker öffentlich |
| `MultiPlayerGameMode.ensureHasSentCarriedItem()` | **private** | Hotbar-Slot an den Server synchronisieren |
| Felder `isDestroying`, `destroyDelay`, `destroyProgress` | **private** | Wurst macht sie per Access-Widener zugänglich |
| `MultiPlayerGameMode.handleContainerInput(int containerId, int slot, int button, ContainerInput, Player)` | public | Inventarklick, sendet `ServerboundContainerClickPacket`. **In 26.3 heißt das nicht mehr `handleInventoryMouseClick`/`ClickType`** |
| `ContainerInput` (Enum, `net.minecraft.world.inventory`) | – | `PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL` |
| `ServerboundPlayerActionPacket(Action, BlockPos, Direction[, int sequence])` | public | `Action.START_DESTROY_BLOCK`, `ABORT_DESTROY_BLOCK`, `STOP_DESTROY_BLOCK`, … |
| `AbstractContainerScreen.slotClicked(Slot, int, int, ContainerInput)` | protected | Klick über den Screen. Wurst macht es per Access-Widener öffentlich |
| `Inventory.getSelectedSlot()` / `setSelectedSlot(int)` | public | Hotbar-Slot lesen und setzen |
| `ItemStack.getDestroySpeed(BlockState)`, `Player.getDestroySpeed(BlockState)` | public | Werkzeugbewertung, Insta-Break-Rechnung |

Zugriffsfreigaben: Meteor [`meteor-client.classtweaker` Z. 33](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/resources/meteor-client.classtweaker#L33) (startPrediction), Wurst [`wurst.accesswidener` Z. 7 und 34–36](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/resources/wurst.accesswidener#L34-L36) (slotClicked, isDestroying/destroyDelay/destroyProgress).

### 2.2 Nuker

**Meteor** ([Nuker.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Nuker.java)):
- Einstellungen: `shape`, `mode`, `range`, `up/down/left/right/forward/back`, `delay`, `max-blocks-per-tick`, `sort-mode`, `packet-mine`, `only-suitable-tools`, `interact`, `rotate`, `list-mode` + `whitelist`/`blacklist`, `swing`.
- Pro Tick bricht es bis zu `max-blocks-per-tick` Blöcke ab. Bei einem Block, der nicht sofort bricht, ist Schluss, außer `packet-mine` ist an ([Z. 486–501](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Nuker.java#L486-L501)).
- `breakBlock` hat drei Pfade ([Z. 512–528](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Nuker.java#L512-L528)):
  - Interact: `BlockUtils.interact`.
  - Packet-Mine: `mc.gameMode.startPrediction(…, seq -> new ServerboundPlayerActionPacket(START_DESTROY_BLOCK, pos, dir, seq))`, direkt danach dasselbe mit `STOP_DESTROY_BLOCK`.
  - Normal: `BlockUtils.breakBlock` → `isDestroying() ? continueDestroyBlock : startDestroyBlock` ([BlockUtils.java Z. 240–262](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/utils/world/BlockUtils.java#L240-L262)).

**Wurst** ([NukerHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/NukerHack.java), Einstellungen in [CommonNukerSettings.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/nukers/CommonNukerSettings.java): Range 1–6, Shape, Flat mode, Mode (ID/MultiID/…), Lock ID):
- Im Survival-Modus wird pro Tick ein Block bearbeitet: Rotation per Paket fälschen, dann `MC.gameMode.continueDestroyBlock(pos, side)` ([Z. 154–163](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/NukerHack.java#L154-L163)).
- Im Creative-Modus und in **SpeedNuker** kommt `BlockBreaker.breakBlocksWithPacketSpam` zum Einsatz: Für jeden Block werden `START_DESTROY_BLOCK` und `STOP_DESTROY_BLOCK` direkt über `connection.send` geschickt, mit dem Konstruktor ohne Sequenznummer ([BlockBreaker.java Z. 169–193](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/util/BlockBreaker.java#L169-L193)). Das hilft nur bei Blöcken, die sofort brechen (Creative, Insta-Mine).

### 2.3 AutoMine / Excavator / Tunneller / InfinityMiner

| Modul | Umsetzung | Quelle |
|---|---|---|
| Wurst **AutoMine** („mines any block that you look at“) | Erst `autoToolHack.equipIfEnabled(pos)`, dann `if(!im.isDestroying()) im.startDestroyBlock(...)`, dann `im.continueDestroyBlock(...)` und `keyAttack.setDown(true)`. Option „Super fast mode“ | [AutoMineHack.java Z. 67–110](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoMineHack.java#L67-L110) |
| Wurst **Excavator** („breaks all blocks in the selected area“) | Eigener `ExcavatorPathFinder` (Wursts eigene KI, **kein Baritone**). Bricht mit `BlockBreaker.breakOneBlock` oder im Creative per Packet-Spam. Range 2–6 | [ExcavatorHack.java Z. 337–370, 391](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/ExcavatorHack.java#L337-L370) |
| Wurst **Tunneller** | Gräbt einen Tunnel mit `continueDestroyBlock`, setzt Fackeln („Place torches“) und wählt Slots per `setSelectedSlot`. Option „Limit“. Die Beschreibung warnt vor Lava und Tod | [TunnellerHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/TunnellerHack.java) |
| Meteor **Excavator** | Reicht die Auswahl an Baritone weiter: `getSelectionManager().addSelection(start,end)` + `getBuilderProcess().clearArea(start,end)`. Nur mit Baritone verfügbar | [Excavator.java Z. 126–127](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Excavator.java#L126-L127) |
| Meteor **InfinityMiner** | Ruft `baritone.getMineProcess().mine(targetBlocks)` auf. Einstellungen: `target-blocks`, `target-items`, `repair-blocks`, `repair-threshold`, `mine-threshold`, `walk-home`, `log-out` | [InfinityMiner.java Z. 205](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/InfinityMiner.java#L205) |
| Meteor **VeinMiner** | Baut zusammenhängende Blöcke ab. Einstellungen: `blocks`, `mode`, `depth`, `delay`, `rotate` | [VeinMiner.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/world/VeinMiner.java) |

Fazit: Bei Meteor ist Baritone der „Bagger“, Wurst hat dafür eine eigene Pfadfindung.

### 2.4 ChestStealer

**Meteor** hat kein eigenes Stealer-Modul. Das steckt in **InventoryTweaks**, Gruppen „Steal and Dump“ und „Auto Steal“ ([InventoryTweaks.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/InventoryTweaks.java)):
- Einstellungen: `steal-screens` (Liste von `MenuType`), `inventory-buttons`, `steal-drop`, `steal-filter` + `steal-items`, `auto-steal`, `auto-dump`, `delay`, `initial-delay`, `random`.
- `moveSlots` läuft auf einem Hintergrund-Thread (`MeteorExecutor`), wartet per `Thread.sleep(delay)` und ruft pro Slot `InvUtils.shiftClick().slotId(i)` auf ([Z. 412–458](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/InventoryTweaks.java#L412-L458)). Das heißt am Ende `mc.gameMode.handleContainerInput(containerMenu.containerId, id, 0, ContainerInput.QUICK_MOVE, mc.player)` ([InvUtils.java Z. 187–190, 353–354](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/utils/player/InvUtils.java#L353-L354)).

**Wurst AutoSteal** ([AutoStealHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoStealHack.java)):
- Einstellungen: „Delay“, „Steal/Store buttons“, „Reverse steal order“.
- Ausgelöst wird es aus Screen-Mixins in `init()` ([ContainerScreenMixin.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/autosteal/ContainerScreenMixin.java), ShulkerBoxScreenMixin).
- Dann läuft ein Thread, der Slots durchgeht, `Thread.sleep(delay)` macht und `screen.slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE)` aufruft ([Z. 70–97](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoStealHack.java#L70-L97)). Er bricht ab, sobald der Screen geschlossen ist.

### 2.5 AutoTool

**Meteor** ([AutoTool.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoTool.java)):
- Einstellungen: `prefer` (Silk/Fortune), `silk-touch-for-ender-chest`, `fortune-for-ores-and-crops`, `anti-break` + `anti-break-percentage`, `switch-back`, `switch-delay`, Whitelist/Blacklist.
- Auslöser ist `StartBreakingBlockEvent`. Den feuert ein `@Inject` an `HEAD` von `startDestroyBlock` ([MultiPlayerGameModeMixin.java Z. 77–79](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/MultiPlayerGameModeMixin.java#L77-L79)).
- Bewertung: `getScore` mit `itemStack.getDestroySpeed(state) * 1000` plus Verzauberungen ([Z. 198–219](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoTool.java#L198-L219)).
- Wechsel: `InvUtils.swap` → `getInventory().setSelectedSlot(slot)` + `ensureHasSentCarriedItem()` über einen Invoker ([InvUtils.java Z. 148–157](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/utils/player/InvUtils.java#L148-L157), [Mixin Z. 162–163](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/MultiPlayerGameModeMixin.java#L162-L163)).

**Wurst** ([AutoToolHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoToolHack.java)):
- Einstellungen: „Use swords“, „Use hands“, „Repair mode“, „Switch back“.
- Auslöser ist `BlockBreakingProgressEvent` von einem Inject in `continueDestroyBlock` ([MultiPlayerGameModeMixin.java Z. 53–57](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/MultiPlayerGameModeMixin.java#L53-L57)).
- Bewertet wird mit `stack.getDestroySpeed(state)`, gewechselt mit `setSelectedSlot`. Der Slot wird über `ensureHasSentCarriedItem` synchronisiert, das `continueDestroyBlock` selbst aufruft.
- Beschädigte Werkzeuge räumt es per `windowClick_QUICK_MOVE` weg (`handleContainerInput`, [Z. 222–240](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoToolHack.java#L222-L240)).

---

## 3. Dupes: Faktenstand (keine Anleitungen)

- **Mojang behebt Item-Dupes als normale Bugs**, belegt durch die „Fixed bugs“-Listen der Versionen, z. B.:
  - [MC-269686](https://bugs.mojang.com/browse/MC-269686) „Oversized music disc stacks can be duplicated with jukeboxes“, behoben in 1.21 ([Wiki: Java Edition 1.21](https://minecraft.wiki/w/Java_Edition_1.21)).
  - [MC-59471](https://bugs.mojang.com/browse/MC-59471) „String can be duplicated using tripwire hooks“, behoben in 1.21.2 ([Wiki: Java Edition 1.21.2](https://minecraft.wiki/w/Java_Edition_1.21.2)).
- **In 26.1, 26.2 und 26.3** enthalten die Fixed-Bugs-Listen keinen Eintrag zu Item-Duplizierung. Geprüft per Textsuche nach „dup“/„duplicat“ im Wiki-Quelltext von [26.1](https://minecraft.wiki/w/Java_Edition_26.1), [26.2](https://minecraft.wiki/w/Java_Edition_26.2) und [26.3](https://minecraft.wiki/w/Java_Edition_26.3). Die Treffer betreffen nur Datapack-Felder und Zielselektoren. Eine Abfrage der Mojira-Suche ohne Login lieferte keine Ergebnisse. Ob es offene, öffentlich bestätigte Dupe-Tickets für 26.x gibt, ist damit **nicht belegt**.
- **Server-Software entscheidet mit.** Paper schaltet bekannte Vanilla-Exploits standardmäßig ab. Sie lassen sich nur über `unsupported-settings` wieder erlauben ([Paper Global Configuration](https://docs.papermc.io/paper/reference/global-configuration/)):
  - `allow-piston-duplication` (Default `false`, „duplication of TNT, carpets, and rails“)
  - `allow-unsafe-end-portal-teleportation` (`false`, „for example sand duplication“)
  - `skip-tripwire-hook-placement-validation` (`false`, „tripwire hook duping“)
  - `allow-permanent-block-break-exploits` (`false`)

  Ob ein Dupe geht, hängt also von Server-Software, Version, Konfiguration und Plugins ab. Auf 2b2t-artigen Servern mit eigener, gepatchter Software sind öffentlich bekannte Dupes typischerweise kurzlebig.
- **Fazit:** Es gibt keinen belegten, universell funktionierenden, rein clientseitigen Dupe. Ein Dupe ist immer ein Fehler in der **Server**-Logik, denn der Server ist für Inventare maßgeblich. Ein Client-Mod kann ihn höchstens auslösen, aber nicht allgemein „bereitstellen“. Ein Dupe-Modul in Cheecky lohnt sich daher nicht.

---

## 4. Empfehlung: bauen vs. installieren

**Installieren:**
- **Baritone `v1.20.0`** für MC 26.3 von [GitHub-Releases](https://github.com/cabaletta/baritone/releases/tag/v1.20.0) in den `mods`-Ordner des CurseForge-Profils legen.
  - `baritone-standalone-fabric-1.20.0.jar`, wenn nur Chat-Befehle genutzt werden.
  - `baritone-api-fabric-1.20.0.jar`, wenn Cheecky die API ansprechen soll.
  - Nicht mit Meteor kombinieren. Wer Meteor nutzt, nimmt Meteors Fork `baritone-meteor`.
- Damit erledigt Baritone `#mine`, `#tunnel`, Auswahl + Freiräumen und `#farm`. Eigenes Pathfinding lohnt sich nicht.

**In Cheecky selbst bauen** (klein, gut verstanden, keine externe Abhängigkeit):
1. **AutoTool:** Mixin an `HEAD` von `MultiPlayerGameMode.startDestroyBlock`, bestes Hotbar-Werkzeug per `ItemStack.getDestroySpeed`, `Inventory.setSelectedSlot`. Den Sync übernimmt `continueDestroyBlock` über `ensureHasSentCarriedItem`.
2. **ChestStealer:** Beim Öffnen eines `AbstractContainerScreen` die Container-Slots per `gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.QUICK_MOVE, player)` mit konfigurierbarer Verzögerung durchgehen. Lieber im Client-Tick takten als auf einem eigenen Thread mit `sleep` wie Meteor und Wurst, dann bleibt alles auf dem Render-Thread.
3. **Nuker (Survival):** Blöcke in Reichweite filtern und sortieren, pro Tick `startDestroyBlock`/`continueDestroyBlock` auf den nächsten Block. Optional „Insta-Break“: mehrere Blöcke pro Tick, wenn sie sofort brechen (Meteor-Logik `canInstaBreak`).
4. **Optional VeinMiner/AutoMine:** gleiche Bausteine wie der Nuker.

**Nicht bauen:** einen eigenen Excavator oder Tunneller mit Pfadfindung (das kann Baritone) und Dupe-Module (siehe Abschnitt 3).
