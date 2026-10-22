# Fabric-Client-Mod (Utility-Module) – Recherche

Stand: 2026-10-22. Ziel: eigener clientseitiger Fabric-Mod mit den Modulen X-Ray, AntiKnockback (Velocity), KillAura, BunnyHop, Fullbright, Fly, NoFall, AutoSprint – für Singleplayer und Server, die das erlauben.
**Nicht im Umfang:** Anti-Cheat-Umgehung. Meteor und Wurst haben dafür eigene Optionen (z. B. „Anti-Kick“ in Flight). Diese werden hier bewusst nicht beschrieben.

Alle Quellcode-Links sind Permalinks auf die gelesenen Commits:

| Repo | Commit (Datum) |
|---|---|
| FabricMC/fabric-example-mod (Branch `26.3`) | [`abe819e`](https://github.com/FabricMC/fabric-example-mod/tree/abe819e940d7a52cdd4630816b894f302e454ae1) (2026-10-17) |
| FabricMC/fabric (Fabric API, Branch `26.3`) | [`a335c35`](https://github.com/FabricMC/fabric/tree/a335c35c0b9a489c7f4e5dd0fe36f1974845f1e1) (2026-10-19) |
| FabricMC/fabric-docs | [`b98d7f7`](https://github.com/FabricMC/fabric-docs/tree/b98d7f7467c532e2d539a191e73ea66c10f0d9c4) (2026-10-21) |
| MeteorDevelopment/meteor-client | [`1532c9e`](https://github.com/MeteorDevelopment/meteor-client/tree/1532c9e2191260d4dc80be1889355e2dab442c15) (2026-10-22) |
| Wurst-Imperium/Wurst7 | [`c040d7a`](https://github.com/Wurst-Imperium/Wurst7/tree/c040d7accda6d270707ac257f906d7387a495af3) (2026-10-21) |

---

## 1. Versionen (Stand 2026-10-22)

| Komponente | Version | Quelle |
|---|---|---|
| Minecraft (neueste stabile Version) | **26.3** (26.4 gibt es nur als Snapshot, aktuell `26.4-snapshot-5`) | [meta.fabricmc.net/v2/versions/game](https://meta.fabricmc.net/v2/versions/game) |
| Fabric Loader | **0.19.5** (Template; laut Meta die neueste als `stable` markierte Version). 0.20.0/0.20.1 existieren, sind aber nicht als stable markiert | [Template gradle.properties](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/gradle.properties), [meta loader](https://meta.fabricmc.net/v2/versions/loader) |
| Fabric API | **0.163.0+26.3** (Template). Neuester 26.3-Build auf Maven: `0.164.0+26.3` | [Template gradle.properties](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/gradle.properties), [Maven-Metadaten](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml) |
| Loom | **1.18-SNAPSHOT** (Template), Plugin-ID `net.fabricmc.fabric-loom`. Neuestes Release auf Maven: `1.19.2`, Wurst nutzt `1.19-SNAPSHOT` | [Template build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/build.gradle), [Loom-Maven](https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml), [Wurst gradle.properties](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/gradle.properties) |
| Gradle | **9.7.1** (Wrapper im Template) | [gradle-wrapper.properties](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/gradle/wrapper/gradle-wrapper.properties) |
| Java | **25** (`options.release = 25`, `JavaVersion.VERSION_25`, `"java": ">=25"`, Mixin `JAVA_25`) | [build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/build.gradle), [fabric.mod.json](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/src/main/resources/fabric.mod.json) |
| Mappings | **Keine.** Seit 26.1 liefert Mojang den Code unverschleiert aus, also arbeitet man direkt mit Mojangs offiziellen Namen. Yarn wird seit 1.21.11 nicht mehr gepflegt. Das Template hat keine `mappings`-Zeile. | [Template build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/build.gradle), [Docs: Migrating Mappings](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/porting/mappings/index.md), [Blog: Removing Obfuscation](https://fabricmc.net/2025/10/31/obfuscation.html) |

Zum Abgleich: Meteor baut auf MC 26.3, Loader 0.19.5, Fabric API 0.162.0+26.3, Loom 1.18-SNAPSHOT und JDK 25 ([libs.versions.toml](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/gradle/libs.versions.toml)). Wurst baut auf MC 26.3, Loader 0.20.1, Fabric API 0.164.0+26.3 und Loom 1.19-SNAPSHOT ([gradle.properties](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/gradle.properties)).
Der Fabric-Blogpost zu 26.3 empfiehlt „Loom 1.17 und Gradle 9.6.0 (at the time of writing)“ und Loader 0.19.5 ([fabricmc.net/2026/09/15/263.html](https://fabricmc.net/2026/09/15/263.html)). Das Template ist inzwischen neuer als diese Angabe.
**Empfehlung:** Die Werte aus dem Template übernehmen (`26.3` / `0.19.5` / `0.163.0+26.3` / `1.18-SNAPSHOT` / Java 25).

---

## 2. Projekt-Setup (Template `fabric-example-mod`, Branch 26.3)

Struktur ([Repo-Baum](https://github.com/FabricMC/fabric-example-mod/tree/abe819e940d7a52cdd4630816b894f302e454ae1)):

```
build.gradle, settings.gradle, gradle.properties, gradle/wrapper/…
src/main/java/com/example/ExampleMod.java              (main-Entrypoint)
src/main/java/com/example/mixin/ExampleMixin.java
src/main/resources/fabric.mod.json, modid.mixins.json, assets/modid/icon.png
src/client/java/com/example/client/ExampleModClient.java  (client-Entrypoint)
src/client/java/com/example/client/mixin/ExampleClientMixin.java
src/client/resources/modid.client.mixins.json
```

- **build.gradle** ([Datei](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/build.gradle)): `id 'net.fabricmc.fabric-loom'` und `loom { splitEnvironmentSourceSets() … }`, das trennt die Source-Sets `main` und `client`. Abhängigkeiten werden mit normalem `implementation` eingebunden:
  ```groovy
  minecraft "com.mojang:minecraft:${project.minecraft_version}"
  implementation "net.fabricmc:fabric-loader:${project.loader_version}"
  implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
  ```
  Seit 26.1 wird nicht mehr remappt. Statt `modImplementation` und `remapJar` nimmt man `implementation` und `jar` ([Blog 26.1, „Unobfuscated Loom“](https://fabricmc.net/2026/03/14/261.html)).
- **gradle.properties** ([Datei](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/gradle.properties)): `minecraft_version`, `loader_version`, `loom_version`, `fabric_api_version`, `version`, `group`.
- **fabric.mod.json** ([Datei](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/src/main/resources/fabric.mod.json)): Darin stehen die `entrypoints` `main` und `client`, die `mixins` (das Client-Mixin-Config hat `"environment": "client"`) und `depends` (`fabricloader >=0.19.5`, `minecraft ~26.3`, `java >=25`, `fabric-api *`). Für einen reinen Client-Mod: `"environment": "client"` setzen und den `main`-Entrypoint weglassen. Das ist eine Ableitung und steht so nicht im Template.
- **Client-Entrypoint** ([ExampleModClient.java](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/src/client/java/com/example/client/ExampleModClient.java)): `implements ClientModInitializer` mit `onInitializeClient()`.
- **Mixin-Config** ([modid.client.mixins.json](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/src/client/resources/modid.client.mixins.json)):
  ```json
  { "required": true, "package": "com.example.client.mixin", "compatibilityLevel": "JAVA_25",
    "client": ["ExampleClientMixin"], "injectors": { "defaultRequire": 1 },
    "overwrites": { "requireAnnotations": true } }
  ```
  Beispiel-Mixin: `@Mixin(Minecraft.class)` mit `@Inject(at = @At("HEAD"), method = "run")` ([ExampleClientMixin.java](https://github.com/FabricMC/fabric-example-mod/blob/abe819e940d7a52cdd4630816b894f302e454ae1/src/client/java/com/example/client/mixin/ExampleClientMixin.java)). Der Klassenname `net.minecraft.client.Minecraft` ist schon der Mojang-Name.
- **Zugriff auf private Felder:** Dafür gibt es Accessor/Invoker-Mixins ([Docs: accessors.md](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/mixins/accessors.md)) oder Class Tweaker / Access Widener ([Docs: class-tweakers](https://github.com/FabricMC/fabric-docs/tree/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/class-tweakers)). Meteor nutzt `"accessWidener": "meteor-client.classtweaker"` ([fabric.mod.json](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/resources/fabric.mod.json)).
- **MixinExtras** (`@ModifyReturnValue`, `@WrapOperation`, `@ModifyExpressionValue`) ist in Fabric Loader enthalten ([fabric-loader build.gradle](https://github.com/FabricMC/fabric-loader/blob/master/build.gradle), `mixinextras-fabric`). Meteor und Wurst nutzen es durchgehend. Die Grundlagen zu Mixins stehen im [SpongePowered/Mixin-Wiki](https://github.com/SpongePowered/Mixin/wiki).

---

## 3. Architektur

### 3.1 Fabric-API-Bausteine (Mojang-Namen!)
- **Keybinds:** In 26.x heißt die Klasse `KeyMappingHelper.registerKeyMapping(KeyMapping)` (Paket `net.fabricmc.fabric.api.client.keymapping.v1`). Das frühere `KeyBindingHelper` gibt es nicht mehr ([KeyMappingHelper.java](https://github.com/FabricMC/fabric/blob/a335c35c0b9a489c7f4e5dd0fe36f1974845f1e1/fabric-key-mapping-api-v1/src/client/java/net/fabricmc/fabric/api/client/keymapping/v1/KeyMappingHelper.java)). Kategorien legt man mit `KeyMapping.Category.register(Identifier)` an.
- **Tick:** `ClientTickEvents.START_CLIENT_TICK` / `END_CLIENT_TICK` (plus `START/END_LEVEL_TICK`) ([ClientTickEvents.java](https://github.com/FabricMC/fabric/blob/a335c35c0b9a489c7f4e5dd0fe36f1974845f1e1/fabric-lifecycle-events-v1/src/client/java/net/fabricmc/fabric/api/client/event/lifecycle/v1/ClientTickEvents.java)).

Offizielles Beispiel aus den Docs ([ExampleModKeyMappingsClient.java](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/reference/latest/src/client/java/com/example/docs/keymapping/ExampleModKeyMappingsClient.java), [key-mappings.md](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/key-mappings.md)):
```java
KeyMapping.Category CATEGORY = KeyMapping.Category.register(ExampleMod.id("custom_category"));
KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
    "key.example-mod.send_to_chat", InputConstants.Type.KEYSYM, InputConstants.KEY_J, CATEGORY));
ClientTickEvents.END_CLIENT_TICK.register(client -> {
    while (key.consumeClick()) { /* toggle */ }
});
```

### 3.2 Vorschlag für ein minimales Modulsystem (eigener Entwurf)
```java
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();
    private boolean enabled; public final String name; public KeyMapping key;
    public void toggle() { enabled = !enabled; if (enabled) onEnable(); else onDisable(); }
    public boolean isEnabled() { return enabled; }
    protected void onEnable() {} protected void onDisable() {}
    public void onTick() {}               // nur aufgerufen, wenn enabled && mc.player != null
}
// Client-Init: Module-Liste anlegen, pro Modul KeyMapping registrieren,
// END_CLIENT_TICK: consumeClick() -> toggle(); danach aktive Module ticken.
```
Mixins fragen den Zustand über eine statische Registry ab, etwa `Modules.get(Velocity.class).isEnabled()`. Für die Konfiguration reicht eine JSON-Datei (Gson ist in Minecraft enthalten) unter `FabricLoader.getInstance().getConfigDir()`. Darin stehen Aktiv-Status, Keycode und Einstellungen. Diese Konfig ist ein eigener Entwurf und orientiert sich an Wurst, s. u.

### 3.3 So machen es Meteor und Wurst
- **Meteor:** Das abstrakte [`Module`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/Module.java) bietet `onActivate/onDeactivate/toggle/enable/disable/isActive` und hat ein eigenes `Keybind`-Feld. Beim Aktivieren meldet sich das Modul selbst am Event-Bus (Orbit) an (`EVENT_BUS.subscribe(this)`), beim Deaktivieren wieder ab. Event-Handler werden mit `@EventHandler` markiert.
  - Ticks kommen aus [`MinecraftMixin`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/MinecraftMixin.java#L121) (`TickEvent.Pre/Post`).
  - Pakete kommen aus [`ConnectionMixin`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/ConnectionMixin.java#L48) (`PacketEvent.Receive/Send`, beide abbrechbar).
  - Tasten verarbeitet [`Modules.onKey`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/Modules.java#L268). Meteor nutzt also keine Vanilla-`KeyMapping`s pro Modul.
  - Gespeichert wird als NBT über [`System.save/load`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/System.java).
- **Wurst:** Eine [`Hack`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hack/Hack.java#L84) erbt von `Feature`. `setEnabled()` ruft `onEnable/onDisable` auf, und dort registriert sich der Hack bei [`EventManager`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/event/EventManager.java) über Listener-Interfaces, z. B. `EVENTS.add(UpdateListener.class, this)`.
  - `UpdateEvent` feuert in [`LocalPlayerMixin`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/LocalPlayerMixin.java) vor `AbstractClientPlayer.tick()`.
  - Keybinds laufen über [`keybinds/KeybindProcessor`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/keybinds/KeybindProcessor.java), der seine Events aus `KeyboardHandlerMixin` bekommt.
  - Welche Hacks aktiv sind, speichert [`EnabledHacksFile`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hack/EnabledHacksFile.java) als JSON.

---

## 4. Module im Einzelnen

Namensschema: **Mojang (26.3, gilt jetzt)** / *Yarn (≤1.21.11, nur zum Lesen alter Tutorials)*. Die Yarn-Namen sind gegen [yarn@1.21.11/mappings](https://github.com/FabricMC/yarn/tree/1.21.11/mappings) geprüft.

### 4.1 AntiKnockback / Velocity
- **Meteor** ([Velocity.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Velocity.java#L152)):
  - Normaler Rückstoß: Im `PacketEvent.Receive` prüft Meteor auf `ClientboundSetEntityMotionPacket` mit `packet.id() == mc.player.getId()`. Dann ersetzt es per Accessor `packet.movement()` durch `alt + (neu − alt) × Faktor`.
  - Explosionen: [`ClientPacketListenerMixin.handleExplosion`](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/ClientPacketListenerMixin.java#L117) skaliert `ClientboundExplodePacket.playerKnockback()`.
- **Wurst:** [`LocalPlayerMixin.lerpMotion(Vec3)`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/antiknockback/LocalPlayerMixin.java) wird überschrieben, und `@ModifyExpressionValue` auf `playerKnockback()` in [`ClientPacketListener.handleExplosion`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/antiknockback/ClientPacketListenerMixin.java). Beide gehen an `modifyKnockback()` in [AntiKnockbackHack](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AntiKnockbackHack.java) (`vec.multiply(1-h, 1-v, 1-h)`).
- Namen: `ClientPacketListener` / *ClientPlayNetworkHandler*, `ClientboundSetEntityMotionPacket` / *EntityVelocityUpdateS2CPacket*, `ClientboundExplodePacket` / *ExplosionS2CPacket*, `handleExplosion` / *onExplosion*, `lerpMotion` / *setVelocityClient*.
- **Für den Einstieg reicht der Wurst-Weg:** ein Override von `lerpMotion` und ein `@ModifyExpressionValue`.

### 4.2 X-Ray
- **Meteor** ([Xray.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Xray.java#L93)):
  - `@ModifyReturnValue` auf `ModelBlockRenderer.shouldRenderFace` ([Mixin](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/ModelBlockRendererMixin.java#L96)) und `@Inject` auf `Block.shouldRenderFace` ([BlockMixin](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/BlockMixin.java#L49)): Flächen von Whitelist-Blöcken werden immer gezeichnet, andere Blöcke werden weggelassen bzw. per Alpha ausgeblendet (`tesselateFlat/tesselateAmbientOcclusion` abbrechen).
  - Die Chunk-Occlusion wird deaktiviert (`ChunkOcclusionEvent` abbrechen), und das Lightmap-Rendering wird auf volle Helligkeit gezwungen ([LightmapMixin](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/LightmapMixin.java)).
  - Beim An- und Ausschalten müssen die Chunks neu gebaut werden: **`mc.levelExtractor.allChanged()`**. Das ist der 26.3-Weg, früher hieß es *`worldRenderer.reload()`*.
- **Wurst** ([XRayHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/XRayHack.java#L116)): `@WrapOperation` auf `ModelBlockRenderer.shouldRenderFace(...)` ([Mixin](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/xray/ModelBlockRendererMixin.java)), außerdem `BlockStateBase.getShadeBrightness`, `VisGraphEvent` (Occlusion aus) und Gamma 16 über `forceSet`. Auch hier `MC.levelExtractor.allChanged()` in `onEnable/onDisable`.
- Sodium bringt einen eigenen Renderer mit. Meteor und Wurst haben dafür separate Mixins (`mixin/sodium/…`, `mixin/xray/sodium/…`). Die eigene X-Ray wirkt mit Sodium also erst, wenn man auch dort Mixins schreibt.
- Namen: `Block.shouldRenderFace` / *Block.shouldDrawSide*.

### 4.3 Fullbright
- **Wurst** ([FullbrightHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/FullbrightHack.java#L100)) setzt `MC.options.gamma()` auf 16. Weil `OptionInstance` Werte über 1.0 sonst ablehnt, braucht es dafür einen eigenen `forceSet`, der den Wert direkt setzt ([OptionInstanceMixin](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/mixin/OptionInstanceMixin.java#L31)). Beim Ausschalten wird der alte Gamma-Wert wiederhergestellt. Alternativ gibt es einen Night-Vision-Modus.
- **Meteor** ([Fullbright.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Fullbright.java#L91)) hat drei Modi:
  - Gamma: `Lightmap.render` abbrechen und die Lightmap-Textur mit 1 füllen ([LightmapMixin](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/mixin/LightmapMixin.java)),
  - Luminance: Mindest-Lichtlevel, danach `allChanged()`,
  - Potion: Night Vision.
- Namen: `Lightmap` / *LightmapTextureManager*, `Options.gamma()` / *GameOptions.getGamma()*.

### 4.4 NoFall
- **Meteor** ([NoFall.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/NoFall.java)), Packet-Modus: In `PacketEvent.Send` wird bei jedem `ServerboundMovePlayerPacket` per Accessor `onGround = true` gesetzt, sobald `getDeltaMovement().y < -0.5` und der Spieler nicht im Kreativmodus ist (`!getAbilities().instabuild`) und nicht mit Elytra fliegt.
- **Wurst** ([NoFallHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/NoFallHack.java#L74)) schickt in jedem Tick zusätzlich `new ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision)` über `MC.player.connection.send(...)`, sofern `fallDistance` über der Schwelle liegt.
- Namen: `ServerboundMovePlayerPacket` / *PlayerMoveC2SPacket*, `.StatusOnly` / *.OnGroundOnly*.

### 4.5 KillAura
- **Meteor** ([KillAura.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/KillAura.java#L286)):
  - Läuft in `TickEvent.Pre`. Ziele kommen aus `TargetUtils.getList(...)` mit `entityCheck`. Geprüft werden: nicht der eigene Spieler, lebendig, Abstand zur Hitbox ≤ Reichweite, Entity-Typ, Sichtlinie, gezähmte und passive Tiere, Freunde, Spieler im Kreativmodus.
  - Timing: `mc.player.getAttackStrengthScale(0.5f) >= 1` (Vanilla-Cooldown) oder eine eigene Verzögerung.
  - Der Angriff selbst: `mc.gameMode.attack(mc.player, target)` und dann ein Hand-Swing ([L460](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/KillAura.java#L460)).
- **Wurst** ([KillauraHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/KillauraHack.java#L145)): Im `onUpdate` werden Ziele aus `EntityUtils.getMeleeAttackableEntities()` nach Reichweite, Sichtfeld und Filtern gefiltert, und mit `min(priority)` wird eins ausgewählt. Der Angriff passiert in `onHandleInput` mit `MC.gameMode.attack(MC.player, target)` und `swing()`. Das Timing steuert [`AttackSpeedSliderSetting`](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/settings/AttackSpeedSliderSetting.java) (bei Einstellung „auto“ wird `getAttackStrengthScale(0) < 1` abgewartet).
- Namen: `MultiPlayerGameMode.attack` / *ClientPlayerInteractionManager.attackEntity*, `mc.gameMode` / *client.interactionManager*, `getAttackStrengthScale` / *getAttackCooldownProgress*, `swing(InteractionHand)` / *swingHand(Hand)*.

### 4.6 BunnyHop
- **Wurst** ([BunnyHopHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/BunnyHopHack.java#L51)):
  ```java
  if (!player.onGround() || player.isShiftKeyDown()) return;
  if (player.isSprinting() && (player.zza != 0 || player.xxa != 0)) player.jumpFromGround();
  ```
  Die Bedingung ist wählbar: Sprinten, Gehen oder immer. Meteor hat kein eigenes BunnyHop-Modul. Ähnliches steckt in `AutoJump` und in den `speed`-Modi.
- Namen: `onGround()` / *isOnGround()*, `jumpFromGround()` / *jump()*, `zza/xxa` / *forwardSpeed/sidewaysSpeed*.

### 4.7 Fly
- **Meteor** ([Flight.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Flight.java#L97)), Modus `Abilities`:
  - `onActivate`: `getAbilities().flying = true; getAbilities().mayfly = true` (`mayfly` nur, wenn nicht `instabuild`).
  - Jeden Tick: `setFlyingSpeed(speed)`.
  - `onDeactivate`: zurücksetzen.
  - Außerdem einen `Velocity`-Modus.
- **Wurst** ([FlightHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/FlightHack.java#L113)), velocity-basiert: Jeden Tick `setDeltaMovement(Vec3.ZERO)` und `abilities.flying = false`. Bei Sprung kommt `+vSpeed` dazu, bei Schleichen `-vSpeed`. Die horizontale Geschwindigkeit kommt über `FlyingSpeedListener`. Wurst hat daneben noch einen separaten `CreativeFlightHack`.
- Namen: `Abilities.mayfly` / *allowFlying*, `instabuild` / *creativeMode*, `getDeltaMovement/setDeltaMovement` / *getVelocity/setVelocity*.
- Hinweis: Der Vanilla-Server wirft Spieler wegen „Flying is not enabled“ raus. Die Anti-Kick-Optionen beider Clients gehören zur Umgehung und werden hier nicht behandelt. Im Singleplayer und mit `allow-flight=true` tritt das Problem nicht auf.

### 4.8 AutoSprint
- **Wurst** ([AutoSprintHack.java](https://github.com/Wurst-Imperium/Wurst7/blob/c040d7accda6d270707ac257f906d7387a495af3/src/main/java/net/wurstclient/hacks/AutoSprintHack.java#L48)): `setSprinting(true)`, sofern kein `horizontalCollision` vorliegt, der Spieler nicht schleicht, nicht schwimmt, nicht unter Wasser ist, `zza > 0` gilt und `input.getMoveVector().length() > 1e-5`.
- **Meteor** ([Sprint.java](https://github.com/MeteorDevelopment/meteor-client/blob/1532c9e2191260d4dc80be1889355e2dab442c15/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Sprint.java#L72)) ruft in `TickEvent.Post` `mc.player.setSprinting(shouldSprint())` auf. Der Strict-Modus prüft `zza > 0.8`, `getFoodData().hasEnoughFood()`, kein flaches Wasser und keine Kollision.
- Simpelste Variante: `mc.options.keySprint.setDown(true)` im Tick. Das ist ein eigener Vorschlag, beide Clients machen es anders.

---

## 5. Stolperfallen

1. **Namen:** Fast alle Tutorials vor 2026 nutzen Yarn-Namen (`MinecraftClient`, `ClientPlayerEntity`, `KeyBindingHelper`, `PlayerMoveC2SPacket` …). Ab 26.1 gelten nur noch die Mojang-Namen: `Minecraft`, `LocalPlayer`, `KeyMappingHelper`, `ServerboundMovePlayerPacket` ([Docs Mappings](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/porting/mappings/index.md)). Alter Code lässt sich mit Loom `migrateMappings` oder dem Ravel-Plugin für IntelliJ umziehen, aber die Mixins muss man trotzdem von Hand nachprüfen (ebd.). Dekompilierten Vanilla-Code zum Nachschlagen gibt es auf [mcsrc.dev](https://mcsrc.dev) ([Blog 26.1](https://fabricmc.net/2026/03/14/261.html)).
2. **Refmap und Remapping:** Das Loom-Plugin `net.fabricmc.fabric-loom` remappt weder Minecraft noch Mods ([Blog 26.1](https://fabricmc.net/2026/03/14/261.html)). Mixin-Ziele sind deshalb einfach die echten Namen und Deskriptoren, z. B. `"handleExplosion(Lnet/minecraft/network/protocol/game/ClientboundExplodePacket;)V"`. Eine Refmap braucht man daher praktisch nicht mehr. Das ist eine Schlussfolgerung, keine wörtliche Aussage der Quelle. `defaultRefmapName` und `useLegacyMixinAp` sind nur noch für das alte Remap-Plugin relevant ([Loom options](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/loom/options.md)). Wer `"defaultRequire": 1` setzt, bekommt einen Absturz, sobald ein Injection-Ziel nicht passt. Das ist gewollt, so fällt der Fehler sofort auf.
3. **API-Änderungen je Version:**
   - 26.1: Plugin-Wechsel auf `net.fabricmc.fabric-loom`, `implementation` statt `modImplementation`, `jar` statt `remapJar`, `HudRenderCallback` entfernt, stattdessen `HudElementRegistry` ([Blog 26.1](https://fabricmc.net/2026/03/14/261.html)).
   - 26.3: **GLFW wurde durch SDL ersetzt**. Das betrifft Code, der Eingaben direkt außerhalb der Minecraft-Klassen abfragt ([Blog 26.3](https://fabricmc.net/2026/09/15/263.html)). Deshalb sollte man auf `KeyMapping` bzw. `InputConstants` setzen, wie im Docs-Beispiel.
   - Rendering ändert sich praktisch in jeder Version. Für 26.3 belegt ist z. B. `levelExtractor.allChanged()`, `Lightmap` ist eine eigene Klasse, und der Block-Renderer heißt `ModelBlockRenderer.shouldRenderFace`. X-Ray und Fullbright brechen deshalb am häufigsten.
4. **IDE und JDK:** Die Gradle-JVM muss Java 25 sein. Für Mixins in IntelliJ braucht man mindestens Version 2025.3 ([Blog 26.1](https://fabricmc.net/2026/03/14/261.html)).
5. **Testen:** `./gradlew runClient` startet den Client in der Dev-Umgebung ([Docs: launching-the-game](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/getting-started/intellij-idea/launching-the-game.md)). Für automatische Tests gibt es `runClientGameTest` ([automatic-testing.md](https://github.com/FabricMC/fabric-docs/blob/b98d7f7467c532e2d539a191e73ea66c10f0d9c4/develop/automatic-testing.md)). Zum Testen am besten eine Singleplayer-Welt verwenden. Bei Paket-Modulen wie NoFall und Velocity laufen die Pakete auch im Singleplayer über den integrierten Server.
6. **Null-Checks:** `mc.player`, `mc.level` und `mc.gameMode` sind im Hauptmenü `null`. Deshalb vor jedem Modul-Tick prüfen. Meteor und Wurst feuern ihre Events nur in der Welt.
7. **Toggle-Seiteneffekte:** Alles, was man beim Aktivieren verändert, muss man beim Deaktivieren zurücksetzen: Gamma, `abilities.flying/mayfly`, neu gebaute Chunks (`allChanged()`). Sonst bleibt der Zustand hängen, siehe die `onDeactivate`-Methoden von Meteor Flight und Fullbright.
8. **Server-Regeln:** Auf fremden Servern nur nutzen, wenn es dort ausdrücklich erlaubt ist.
