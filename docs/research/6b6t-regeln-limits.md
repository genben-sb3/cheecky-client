# 6b6t – Offizielle Regeln, Bewegungs-Limits und Versionen – Recherche

Stand: 2026-10-23. Ziel: herausfinden, welche Regeln und Grenzwerte der Anarchy-Server 6b6t (play.6b6t.org) selbst veröffentlicht. Daraus sollen Standardwerte für die Module des Cheecky Clients (MC 26.3) abgeleitet werden.
**Nicht im Umfang:** Techniken, Configs oder Anleitungen, mit denen man den Anticheat umgeht. Solches Material wurde bewusst nicht gesammelt. Aus öffentlichen Issues des Betreibers stehen hier nur die Grenzwerte, die der Server durchsetzt, nicht wie der Check intern funktioniert.

Alle Quellen wurden am **2026-10-23** abgerufen.

| Quelle | Wer schreibt sie? | Status |
|---|---|---|
| [6b6t.org](https://6b6t.org/) (Startseite und FAQ) | Betreiber | offiziell |
| [6b6t.org/en/rules](https://6b6t.org/en/rules) | Betreiber | offiziell |
| [6b6t.org/en/terms-of-service](https://6b6t.org/en/terms-of-service) (Effective Date 23.02.2026) | Betreiber (qbasty labs FZE) | offiziell |
| [6b6t.org/en/join](https://6b6t.org/en/join), [/en/connection-issues](https://6b6t.org/en/connection-issues), [/en/status](https://6b6t.org/en/status), [/en/mod](https://6b6t.org/en/mod) | Betreiber | offiziell |
| [6b6t.org/en/updates](https://6b6t.org/en/updates) (Spiegel des Discord-Changelog-Kanals) | Betreiber (Posts von „qbasty“) | offiziell |
| [blog.6b6t.org](https://blog.6b6t.org/) | Betreiber | offiziell |
| [github.com/6b6t/feedback](https://github.com/6b6t/feedback) („Invite-only public 6b6t issues/feature request tracker“) in der GitHub-Organisation `6b6t`, verifiziert, verlinkt auf 6b6t.org | Betreiber „qbasty“ und eingeladene Mitwirkende | offiziell, aber **interne Arbeitsnotizen**, keine Regeln |
| [CurseForge: AnarchyMod](https://www.curseforge.com/minecraft/mc-mods/anarchymod), [anarchymod-releases.6b6t.org](https://anarchymod-releases.6b6t.org/), [6b6t.kianbrose.com](https://6b6t.kianbrose.com/AnarchyModReleases/1.4.3/) | Betreiber (die Mirrors sind im offiziellen Changelog verlinkt) | offiziell |

Inoffizielle Quellen (Spieler-Wikis, Reddit, „Bypass“-Guides) sind hier **nicht** ausgewertet.

---

## 1. Grundregeln (offiziell)

- 6b6t bezeichnet sich als „survival Minecraft anarchy server with no rules, no punishments and no queue“ ([6b6t.org](https://6b6t.org/)).
- Laut ToS Abschnitt 4 („In-Game Behaviour (Anarchy Rules)“) sind ausdrücklich erlaubt: „cheating and hacked clients, spamming, griefing and raiding“ ([ToS](https://6b6t.org/en/terms-of-service)).
- **Strikt verboten** sind laut derselben Stelle nur drei Dinge. Verstöße können zum dauerhaften Ausschluss führen ([ToS](https://6b6t.org/en/terms-of-service), [Rules](https://6b6t.org/en/rules)):
  1. „DDoS attacks or attempts to disrupt connectivity of the server, infrastructure or other players“
  2. Doxxing
  3. illegale Inhalte mit Minderjährigen oder Handlungen, die Minderjährige gefährden
- Zusätzlich behält sich der Betreiber vor, Nutzer zu entfernen oder einzuschränken, „whose behaviour makes the Services unusable for others“ ([ToS](https://6b6t.org/en/terms-of-service)). *Ableitung:* Lag-Machines und Crash-Exploits sind nirgends ausdrücklich verboten. Fallen sie aber unter „disrupt connectivity“ oder „unusable for others“, kann der Betreiber nach eigenem Ermessen eingreifen.
- Zu Hacks heißt es: „Technical protections may still limit some client features. Permission to use a client does not mean every exploit will work.“ ([Rules](https://6b6t.org/en/rules)).
- Zu Dupes heißt es: „Duping is part of anarchy gameplay, but exploits may be patched.“ Dupe-Events werden über Updates und Discord angekündigt ([Rules](https://6b6t.org/en/rules)).
- Chat: Ein Anti-Spam-System kann bei störendem Chat und Werbung stummschalten ([Rules](https://6b6t.org/en/rules)).
- Der Betreiber sagt ausdrücklich, dass keine Regeln dazukommen: „You have my word 6B6T IS NOT ADDING RULES.“ (qbasty, 17.09.2026, [Updates](https://6b6t.org/en/updates)).
- **Es gibt keine Liste mit erlaubten oder verbotenen Clients oder Modulen.** Auf keiner offiziellen Seite werden Clients oder Module genannt ([Rules](https://6b6t.org/en/rules), [ToS](https://6b6t.org/en/terms-of-service)).

## 2. Bewegung: Elytra, Speed, Flug, Chunks

**Auf den Regelseiten steht kein Bewegungs-Limit.** Rules, ToS, FAQ und Startseite nennen keine Höchstgeschwindigkeit, keine Elytra- oder Raketenlimits und keine Grenzen fürs Laden von Chunks ([Rules](https://6b6t.org/en/rules), [ToS](https://6b6t.org/en/terms-of-service), [6b6t.org](https://6b6t.org/)).

Öffentlich, aber nur als interne Arbeitsnotiz des Betreibers im Feedback-Tracker:

- **Anticheat:** Der Server nutzt einen eigenen Fork von NoCheatPlus (`6b6t/NoCheatPlus`) auf „Fox“, einem eigenen Folia-Downstream ([Issue #4](https://github.com/6b6t/feedback/issues/4), [Issue #10](https://github.com/6b6t/feedback/issues/10)).
- **Vertikales Elytra-Limit:** Seit dem 13./14.09.2026 gibt es ein „hard vertical cap for gliding“. Eine Gleitbewegung mit mehr als **3,0 Blöcken pro Tick** vertikal (|Δy|, also 60 Blöcke/s) gilt als Verstoß. Ausnahmen gibt es nur bei einer fortgesetzten Abwärtsbewegung. Laut Issue war der Wert am 16.09.2026 „still 3.0“ ([Issue #4](https://github.com/6b6t/feedback/issues/4)). Laut Betreiber-Kommentar soll dieses Limit „ruled in or out with data, not relaxed blindly“ werden. Es kann sich also ändern ([Issue #4, Kommentar 16.09.2026](https://github.com/6b6t/feedback/issues/4)).
- **Horizontal:** Laut Betreiber-Kommentar gilt der allgemeine horizontale Check auch für Elytra-Bewegung. Ein Zahlenwert wird **nicht** genannt ([Issue #4, Kommentar](https://github.com/6b6t/feedback/issues/4)).
- **Hacked-Client-Geschwindigkeiten:** Laut Issue ist offen, ob und bis zu welcher Geschwindigkeit „Volytra-style speeds up to X blocks/s“ erlaubt werden sollen („Decide the policy for hacked-client speeds separately“). Es gibt also **keinen veröffentlichten Grenzwert in Blöcken pro Sekunde** ([Issue #4](https://github.com/6b6t/feedback/issues/4)). Zum Vergleich steht dort, dass ein senkrechter Sturzflug ohne Boost auf etwa **78 Blöcke/s** kommt (−3,92 Blöcke/Tick).
- **Neue Chunks:** Es gibt kein veröffentlichtes Limit für neue gegenüber alten Chunks. Bekannt sind nur Fehlerberichte: Bei Elytra-Flug durch neu generiertes Terrain hörte die Chunk-Generierung nach etwa 1.000 Blöcken auf (Spielerbericht, Juli 2026, geschlossen) ([Issue #1](https://github.com/6b6t/feedback/issues/1)). Spieler werden auf den Backup-Server geworfen, wenn sie in einem noch ladenden Gebiet ankommen (offen) ([Issue #28](https://github.com/6b6t/feedback/issues/28)). Das sind Bugs, keine bewusst gesetzten Limits.

## 3. Weitere veröffentlichte Grenzwerte

| Grenzwert | Wert | Quelle |
|---|---|---|
| End-Kristalle zerstören | max. **10 pro Sekunde** (einer alle 100 ms), vorher 4/s. Per Spieler-Umfrage geändert am 21.10.2026 | [Changelog 2026/10/21](https://6b6t.org/en/updates) |
| Banner pro Chunk | **32** (Standard). Anhebung auf 128 ist beantragt, Issue noch offen | [Issue #9](https://github.com/6b6t/feedback/issues/9) |
| Shulker pro Chunk / Truhen pro Chunk / Block-Entities gesamt pro Chunk | **128 / 512 / 1024** | [Issue #9](https://github.com/6b6t/feedback/issues/9) |
| Max. Spieler | 2000 | [Status](https://6b6t.org/en/status) |

## 4. Versionen – wichtig für MC 26.3

- Die Statusseite zeigt „**6b6t Proxy 26.2**“ als Version ([Status](https://6b6t.org/en/status)). Laut Blog vom 20.10.2026: „6b6t runs on 26.2“ ([Blog: How to Install AnarchyMod](https://blog.6b6t.org/how-to-install-anarchymod/)).
- **Minimum:** 1.21.11. Ältere Clients werden abgewiesen („Unsupported Minecraft version: your Minecraft is 1.21.10 or older“) ([Blog](https://blog.6b6t.org/how-to-install-anarchymod/), [Blog 1.21.11-Update](https://blog.6b6t.org/6b6t-minecraft-1-21-11-update/)).
- **Java braucht AnarchyMod:** Mojang hat 6b6t am 15.07.2026 auf die Blockliste gesetzt. Ohne AnarchyMod wird der Server in Java als offline angezeigt ([Join](https://6b6t.org/en/join), [Connection help](https://6b6t.org/en/connection-issues)). AnarchyMod gibt es nur für Fabric und es braucht keine Fabric API ([Mod](https://6b6t.org/en/mod), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/anarchymod)). Laut offiziellem Blog kann AnarchyMod neben anderen Mods im selben `mods`-Ordner liegen ([Blog](https://blog.6b6t.org/how-to-install-anarchymod/)).
- **26.3 auf der Serverseite:** Laut Betreiber nehmen die Proxys 26.3-Clients (Protokoll 777) seit dem 20.09.2026 über **ViaVersion 5.12.0** an, das 26.3 auf 26.2 (776) übersetzt. Der Proxy-Fork selbst kann nur bis 26.2 ([Issue #20](https://github.com/6b6t/feedback/issues/20), [Issue #21](https://github.com/6b6t/feedback/issues/21)).
- **26.3 auf der Clientseite:** Es gibt **keinen AnarchyMod-Build für 26.3**. Die neueste Version ist 1.4.3 mit Builds für 1.9.4 bis 26.2 ([Release-Verzeichnis](https://6b6t.kianbrose.com/AnarchyModReleases/1.4.3/), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/anarchymod)). Auf dem Cloudflare-Mirror antwortet `anarchymod-mc-26.3-1.4.3.jar` mit HTTP 404, `…-26.2-1.4.3.jar` mit 200 (geprüft am 2026-10-23, [Mirror](https://anarchymod-releases.6b6t.org/)). Issue #20 ist noch offen. Das offizielle Blog schreibt: „Do not pick 26.3 … there is no AnarchyMod file for 26.3 yet.“ ([Blog](https://blog.6b6t.org/how-to-install-anarchymod/)).
- Bekannter Fehler: AnarchyMod 1.4.3 kann auf 26.1.2 beim Login die Verbindung trennen („Failed to encode packet … anarchymod:join“). Workaround des Betreibers: 1.21.11 benutzen ([Issue #24](https://github.com/6b6t/feedback/issues/24)).
- Widerspruch in den offiziellen Quellen: Die Startseite nennt „1.19.4+ Supported“ für AnarchyMod ([6b6t.org](https://6b6t.org/)), CurseForge „1.9.4 bis 26.2“ ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/anarchymod)). Tatsächlich joinen können laut Blog nur Clients ab 1.21.11.

**Folge für Cheecky (MC 26.3):** Der Server würde 26.3 annehmen. Ein 26.3-Client kommt aber wegen Mojangs Blockliste nicht hin, solange es keinen AnarchyMod-Build für 26.3 gibt ([Issue #20](https://github.com/6b6t/feedback/issues/20), [Blog](https://blog.6b6t.org/how-to-install-anarchymod/)). Wer auf 6b6t testen will, muss nach offizieller Empfehlung auf 26.2 (oder 1.21.11 / 26.1.x) gehen, oder auf den 26.3-Build von AnarchyMod warten.

## 5. Was nicht gefunden wurde

- Keine offizielle Höchstgeschwindigkeit in Blöcken/s, weder horizontal, noch für Elytra, Boote oder zu Fuß.
- Kein offizielles Limit für Feuerwerksraketen (Anzahl oder Rate).
- Kein offizielles Limit für das Laden neuer Chunks und keine Regel „langsamer in neuen Chunks“.
- Keine Liste erlaubter oder verbotener Clients oder Module, keine Regel zu Flug allgemein (Creative-Fly, Jetpack usw.).
- Kein ausdrückliches Verbot von Lag-Machines (nur die allgemeinen ToS-Klauseln, siehe Abschnitt 1).
- Die Discord-Regeln stehen nur im Discord ([ToS Abschnitt 5](https://6b6t.org/en/terms-of-service)) und wurden nicht gelesen.

---

## Empfohlene Modul-Standardwerte

Werte gibt es nur, wo ein veröffentlichter Grenzwert existiert. Alle anderen Felder bekommen bewusst keinen 6b6t-spezifischen Wert.

| Modul / Einstellung | Empfehlung | Begründung |
|---|---|---|
| ElytraFly – max. **vertikale** Geschwindigkeit | **≤ 3,0 Blöcke/Tick (≤ 60 Blöcke/s)**, als Standard mit Puffer z. B. **2,5 Blöcke/Tick** | Hartes vertikales Gleit-Limit im NCP-Fork ([Issue #4](https://github.com/6b6t/feedback/issues/4)). Der Wert ist nicht als Regel veröffentlicht und kann sich ändern. |
| ElytraFly – max. **horizontale** Geschwindigkeit | **Kein offizieller Wert bekannt.** Standard am besten auf Vanilla-Niveau lassen (keine Zusatzgeschwindigkeit) | Der Betreiber hat noch nicht entschieden, wie viel er erlaubt ([Issue #4](https://github.com/6b6t/feedback/issues/4)). |
| Speed, Fly, BunnyHop, Boot-Speed | **Kein offizieller Wert bekannt** | Siehe Abschnitt 5. |
| Raketen-Delay | **Kein offizieller Wert bekannt** | Siehe Abschnitt 5. |
| AutoCrystal / CrystalAura – Break-Delay | **≥ 100 ms** (≤ 10 Breaks/s, also ≥ 2 Ticks) | Changelog vom 21.10.2026 ([Updates](https://6b6t.org/en/updates)). |
| Banner-Platzierung (z. B. Auto-Build) | ≤ 32 Banner pro Chunk | Aktueller Standard ([Issue #9](https://github.com/6b6t/feedback/issues/9)). |
| Zielversion zum Testen auf 6b6t | 26.2 (MC 26.3 erst, wenn AnarchyMod 26.3 verfügbar ist) | [Blog](https://blog.6b6t.org/how-to-install-anarchymod/), [Issue #20](https://github.com/6b6t/feedback/issues/20). |

Wichtig: 6b6t verbietet keine Bewegungs-Hacks. Die Werte oben verhindern nur, dass der Anticheat den Spieler zurücksetzt (Setbacks). Strafen gibt es dafür nicht ([ToS](https://6b6t.org/en/terms-of-service)).
