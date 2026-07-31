# dist

Compiled build of the Fabric / Minecraft 26.2 port (source in `../26.2/`).

The jar below is a **ready-to-install build** — nothing needs compiling to play, just drop it into
`mods/`. The sources it was built from live in `../26.2/`; the untouched NeoForge 26.1.2 original
it was ported from is in `../26.1.2/`.

| File | `blockui-26.2-0.0.1.jar` |
|---|---|
| Minecraft | 26.2 |
| Loader | Fabric, loader ≥ 0.19.3 |
| Java | 25 |
| Requires | Fabric API 0.154.2+26.2 or newer |
| sha256 | `6092e4ecb47ac265e9806b2904505ada0ddc2fc2b838088338efefa9a2f69dd8` |

Install: drop the jar and Fabric API into the `mods/` folder of a Fabric 26.2 profile or server.

BlockUI is a **library**. On its own it adds no content — it ships a UI toolkit for other mods
(Structurize, MineColonies) plus a test GUI, and the `com.ldtteam.common` layer those mods import.

## Try it: the test GUI

Press **`Ctrl` + `Alt` + `Shift` + `X`** in game. The `X` is rebindable under
Options → Controls → BlockUI; the three modifiers are hard-coded, as they were on NeoForge.

Known issue: the **“ItemIcon To BlockState”** button builds one picture-in-picture render per
blockstate in the game — **32 366** of them on vanilla 26.2. The renderer carries an upstream
comment saying it *“lags when rendering more than 100 instances”*, so that screen is expected to
be unusable. This is inherited from upstream 26.1.2, not introduced by the port. Every other test
screen works.

## What changed in the port

NeoForge → Fabric, one hop, on an already-unobfuscated 26.1.2 source base.

- **No mixins.** The mod had none and still has none: every NeoForge hook found a real Fabric or
  vanilla API — `AtlasRegistry`, `PictureInPictureRendererRegistry`, vanilla `RenderPipelines.register`,
  `HudElementRegistry`, `ClientHotbarScrollEvents`, `ResourceLoader`, `KeyMappingHelper`.
- **AccessTransformer → AccessWidener**, 14 entries expanded to 19 lines (AccessWidener has no
  wildcards); every target verified present in decompiled 26.2.
- **`com.ldtteam.common` keeps its public shape** — Structurize and MineColonies import it, so the
  network types and method names are unchanged even though the internals moved to
  `PayloadTypeRegistry` / `ServerPlayNetworking` / `PlayerLookup`.

## Deliberately dropped

Read these before filing a bug — they are decisions, not regressions. The full log is
`../26.2/PORT-STATUS.md` → *Disabled content*.

- **Config is in-memory only.** NeoForge `ModConfigSpec` has no Fabric or vanilla counterpart.
  Values hold their old TOML defaults; there is no persistence, no client/server sync and no
  config screen.
- **No GUI layer stack.** Vanilla has no `pushGuiLayer`/`popGuiLayer`, so a layered window
  remembers the screen underneath and restores it on close.
- **Scroll capture misses spectator mode**, and item tooltips no longer honour a per-item custom
  font — both were NeoForge-only extensions.

## Verification status

- `build` green; `validateAccessWidener` green; 22 unit tests pass.
- Dedicated 26.2 server boots: `Done (7.636s)! For help, type "help"`, zero `/ERROR]` lines except
  vanilla's first-run `Failed to load properties from file: server.properties`.
- Client GUI checked by hand: the test window opens and its screens work, except the known issue
  above.

Rebuild with:

```sh
../gradle-dist/install.sh     # vendored Gradle 9.6.1 + Java 25
cd ../26.2
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 /opt/gradle-9.6.1/bin/gradle build --no-daemon
```
