# V1 Host Mod 2.0 (NeoForge 1.21.1)

## Grundfunktionen (wie vorher)
- **V1 Block**: im Survival craftbar (Amethystblock Ecken, Obsidian Seiten, Netherstern Mitte) und mit der Spitzhacke abbaubar.
- **Rechtsklick** oeffnet das V1 Terminal. `v1 host` (mit oder ohne `/`) macht dich zum **Host**:
  Block sitzt als Kopfteil (Fluch der Bindung), dauerhafte Superkraefte, V1 Speer, alles bleibt nach dem Tod.

## NEU in 2.0
- **Level-System (1-10)**: XP durch Kills (Monster nach Leben, Bosse 300, Spieler 25). Jedes Level macht Staerke, Speed, Resistenz, Sprung, Eile, Herzen und Schaden staerker.
- **4 aktive Faehigkeiten** (Tasten im Controls-Menue aenderbar):
  - **R Dash**: Ansturm nach vorn, trifft alle Gegner auf dem Weg
  - **G Schockwelle**: Druckwelle um dich herum
  - **V Overdrive**: 15+ s Raserei: 1,75x Schaden, halber Schaden erhalten, extra Staerke und Speed
  - **B Blink**: Teleport in Blickrichtung (nicht durch Waende)
- **Passive Boni**: ab Lv. 3 nie hungrig, ab Lv. 4 Lebensraub, ab Lv. 6 Dornen-Aura, **Impact-Landung** (ab 4 Block Fall: Schockwelle statt Schaden).
- **HUD** links unten: Level, XP-Balken und Cooldowns der Faehigkeiten.
- **V1 Speer** wird jetzt als echtes Wurfgeschoss mit Flammenspur gerendert. Beim Host **fliegt er automatisch zurueck**. Zusaetzlich ist er craftbar (Netherit-Schwert + Dreizack + Mace, formlos), hat mehr Reichweite und einen Mace-Smash mit Schockwelle.
- **Terminal-Befehle**: `v1 host`, `v1 status`, `v1 spear`, `v1 help`.
- **Chat-Befehle**: `/v1 status`, `/v1 spear`, fuer Ops zusaetzlich `/v1 host [Spieler]`, `/v1 unhost [Spieler]`, `/v1 level <1-10>`.
- Deutsche und englische Sprachdatei.

## Bauen
Siehe BUILD_ONLINE.md (GitHub, ohne Installation) oder `build_mod.bat` (Windows, JDK 21).
Die .jar liegt danach in `build/libs/`. Falls `neo_version` (21.1.172) nicht gefunden wird, nimm eine andere 21.1.x-Version
von https://projects.neoforged.net/neoforged/neoforge in `gradle.properties`.
