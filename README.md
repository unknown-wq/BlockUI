<h1 align="center">
  BlockUI — Fabric port for Minecraft 26.2
</h1>

<p align="center">
  <b>The XML-based GUI library for Minecraft 26.2 on the Fabric loader.</b><br>
  An unofficial community port of <a href="https://github.com/ldtteam/BlockUI">LDTTeam's BlockUI</a>
  from NeoForge to Fabric — the UI toolkit that MineColonies and Structurize are built on.
</p>

<p align="center">
  <img alt="Minecraft 26.2" src="https://img.shields.io/badge/Minecraft-26.2-brightgreen?style=for-the-badge">
  <img alt="Fabric" src="https://img.shields.io/badge/Loader-Fabric%200.19.3-1976d2?style=for-the-badge">
  <img alt="Fabric API" src="https://img.shields.io/badge/Fabric%20API-0.154.2%2B26.2-1976d2?style=for-the-badge">
  <img alt="Java 25" src="https://img.shields.io/badge/Java-25-orange?style=for-the-badge">
  <img alt="License GPL-3.0" src="https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge">
</p>

<p align="center">
  <a href="#-download">Download</a> ·
  <a href="#-the-other-ports">Other ports</a> ·
  <a href="#-what-this-mod-does">What it does</a> ·
  <a href="#-installation">Installation</a> ·
  <a href="#-for-mod-developers">For developers</a> ·
  <a href="#-building-from-source">Build</a> ·
  <a href="#%EF%B8%8F-known-limitations">Limitations</a> ·
  <a href="#-issues-and-bug-reports">Issues</a> ·
  <a href="#-credits-and-license">Credits</a>
</p>

---

## 🧩 The other ports

This repository is one piece of a set. The goal of the whole set is to bring the
**[MineColonies](https://github.com/unknown-wq/minecolonies) mod family to Fabric on Minecraft 26.2**,
and MineColonies does not run alone — it needs its library mods. They are ported bottom-up,
starting with the leaves of the dependency tree, of which this is one.

| Mod | Fabric 26.2 port | Original (upstream) | Role in the stack |
|---|---|---|---|
| **BlockUI** | **you are here** | [ldtteam/BlockUI](https://github.com/ldtteam/BlockUI) | XML-driven GUI framework. No dependencies — ported first |
| **Domum Ornamentum** | [unknown-wq/Domum-Ornamentum](https://github.com/unknown-wq/Domum-Ornamentum) | [ldtteam/Domum-Ornamentum](https://github.com/ldtteam/Domum-Ornamentum) | Skinnable decorative blocks. Also a leaf |
| **Structurize** | [unknown-wq/Structurize](https://github.com/unknown-wq/Structurize) | [ldtteam/Structurize](https://github.com/ldtteam/Structurize) | Schematic placement and building tools |
| **MineColonies** | [unknown-wq/minecolonies](https://github.com/unknown-wq/minecolonies) | [ldtteam/minecolonies](https://github.com/ldtteam/minecolonies) | The colony-building mod itself |

```
BlockUI  ──┐
           ├──> Structurize ──> MineColonies
Domum Ornamentum ──────────────┘
```

> All of these are **ports, not forks with new features**. The API, behaviour and IDs are the
> upstream authors' work; what happens here is the move from **NeoForge to Fabric** and up to
> **Minecraft 26.2**.

---

## 📦 Download

**The built mod jar lives in [`dist/`](dist/).** Grab it there — no build step required.

```
dist/blockui-26.2-0.0.1.jar
```

Drop that file into your `mods/` folder together with the [Fabric API](https://modrinth.com/mod/fabric-api).
See [`dist/README.md`](dist/README.md) for the checksum and build details.

---

## 🪟 What this mod does

BlockUI is a **library mod**. On its own it adds no blocks, items or gameplay — it provides a UI
toolkit that other mods build their screens with, plus the shared `com.ldtteam.common` layer those
mods import.

Screens are **declared in XML** and backed by a `Window` class that handles callbacks and supplies
data, instead of being laid out by hand in Java:

- **Controls** — buttons and image buttons, toggles, checkboxes, text and text fields, scrollbars,
  gradients, tooltips, item icons, entity icons, blockstate icons
- **Views** — windows, groups, boxes, switch views, overlays, dropdown lists, scrolling lists and
  containers, zoom-and-drag views
- **Support layers** — an XML parser and codec (`XmlOps`), a fake-level implementation for
  rendering blocks and blockstates inside a GUI, networking helpers and a language layer

**Try it in game:** press **`Ctrl` + `Alt` + `Shift` + `X`** to open the test window. The `X` is
rebindable under Options → Controls → BlockUI; the three modifiers are hard-coded, as they were on
NeoForge.

**Port facts**

| | |
|---|---|
| Minecraft | 26.2 |
| Loader | Fabric Loader 0.19.3+ |
| Requires | Fabric API 0.154.2+26.2 |
| Java | 25 |
| Environment | client **and** dedicated server (both sides) |
| Mod id | `blockui` |
| Mod version | 0.0.1 |
| License | GPL-3.0-only (same as upstream) |

---

## 🚀 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) 0.19.3 or newer for Minecraft 26.2.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 into your `mods/` folder.
3. Copy the jar from [`dist/`](dist/) into the same `mods/` folder.
4. Launch the game and press `Ctrl` + `Alt` + `Shift` + `X` to confirm it loaded.

BlockUI is required on both client and server, and it has no mod dependencies of its own beyond
Fabric API. On a dedicated server it does almost nothing by itself — it is there for the mods that
depend on it.

---

## 🛠 For mod developers

Two things matter if you are porting a mod that depends on BlockUI:

- **The public API keeps its upstream shape.** Class names, package names and method signatures in
  `com.ldtteam.blockui` and `com.ldtteam.common` are unchanged, even where the internals were
  rewritten for Fabric — so consumer code compiles against the same names it always did.
- **`com.ldtteam.common` ships inside this mod.** It is not a separate library to hunt for: the
  files under `src/main/java/com/ldtteam/common/**` (networking, fake levels, codecs, config,
  language, utilities) live here. Structurize and MineColonies import them from BlockUI, which they
  already declare as a required dependency.

---

## 🔨 Building from source

The build targets **Java 25** and uses **Fabric Loom** on **Gradle 9.6.1**.

```sh
./gradle-dist/install.sh                       # installs Gradle 9.6.1 to /opt and OpenJDK 25
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64

cd 26.2
/opt/gradle-9.6.1/bin/gradle build             # jar lands in 26.2/build/libs/
```

Useful tasks: `runClient`, `runServer`, `test`, `validateAccessWidener`. Minecraft 26.1+ ships
unobfuscated, so the build carries **no mappings line** — Yarn is neither used nor needed.

---

## 🧭 How the port was done

The starting point was upstream's own `port/26` branch, which had already taken the mod to
Minecraft 26.1.2 on NeoForge with Java 25 — the most advanced base of the four mods in the set.
That left one axis to cross: **the loader**, plus the 26.1.2 → 26.2 delta.

- **No mixins.** The mod had none upstream and still has none: every NeoForge hook found a real
  Fabric or vanilla API — `AtlasRegistry`, `PictureInPictureRendererRegistry`, vanilla
  `RenderPipelines.register`, `HudElementRegistry`, `ClientHotbarScrollEvents`, `ResourceLoader`,
  `KeyMappingHelper`.
- **AccessTransformer → AccessWidener** — the upstream wildcard entries expanded by hand, since
  AccessWidener has none, with every target verified present in the decompiled 26.2 sources.
- **Networking rebuilt** on `PayloadTypeRegistry` / `ServerPlayNetworking` / `PlayerLookup`, while
  `com.ldtteam.common`'s public types and method names stayed exactly as they were, so Structurize
  and MineColonies compile against them unchanged.

Status: `build` green, `validateAccessWidener` green, 22 unit tests passing, a dedicated 26.2
server boots to `Done!` with no unexpected errors, and the test GUI opens and works on a real
client. The full record is in [`26.2/PORT-STATUS.md`](26.2/PORT-STATUS.md) and
[`dist/README.md`](dist/README.md).

---

## ⚠️ Known limitations

Some NeoForge-only hooks have no equivalent in Fabric or in vanilla 26.2. These are decisions, not
regressions — please read them before filing a bug.

| Area | What differs from upstream | Impact |
|---|---|---|
| **Config** | NeoForge's `ModConfigSpec` has no Fabric or vanilla counterpart | Config is in-memory only: values keep their old defaults, with no persistence, no client/server sync and no config screen |
| **GUI layer stack** | Vanilla has no `pushGuiLayer`/`popGuiLayer` | A layered window remembers the screen underneath and restores it on close |
| **Scroll capture** | NeoForge-only extension | Spectator mode is not covered |
| **Tooltip fonts** | NeoForge-only extension | Item tooltips no longer honour a per-item custom font |
| **"ItemIcon To BlockState" test screen** | Inherited from upstream, not caused by the port | It renders one picture-in-picture view per blockstate — 32,366 on vanilla 26.2 — and is effectively unusable. Upstream's own comment notes it lags past 100 instances |

---

## 🐞 Issues and bug reports

**Found a problem? [Open an issue](https://github.com/unknown-wq/BlockUI/issues) — please do.**
Bug reports are genuinely welcome; that is how the remaining rough edges get found.

- Report **port bugs here**, not to LDTTeam. Anything caused by the move to Fabric 26.2 is this
  repository's doing, not upstream's.
- Helpful things to include: Minecraft / Fabric Loader / Fabric API versions, the full log
  (`logs/latest.log` or the crash report), the other mods installed, and the steps that
  reproduce it.
- If the same bug also happens on upstream's NeoForge build, it belongs
  [upstream](https://github.com/ldtteam/BlockUI/issues) instead.

---

## 📁 Repository layout

```
.
├── dist/            # ← the built mod jar, ready to drop into mods/
├── 26.2/            # the Fabric 26.2 port — sources, build, port documentation
├── 26.1.2/          # read-only snapshot of upstream port/26 (NeoForge 26.1.2, Java 25) — the port base
├── porting-26.2/    # notes, rename tables and scripts collected while porting
└── gradle-dist/     # vendored Gradle 9.6.1 + toolchain installer
```

Only one snapshot is kept, because there was nothing to choose between: upstream's `port/26`
already contains the whole `version/main` line — the old head is an ancestor of it, with zero
commits missing. The snapshot is upstream code, kept verbatim for reference and diffing; it is not
edited and is not part of the build.

---

## 🙏 Credits and license

**BlockUI is the work of [LDTTeam (Let's Dev Together)](https://github.com/ldtteam)** — the team
behind MineColonies, Structurize and Domum Ornamentum. The framework, its XML system and every line
of its game logic originate with them. All credit for the mod belongs to its original authors and
contributors:

- Upstream source: **[github.com/ldtteam/BlockUI](https://github.com/ldtteam/BlockUI)**
- CurseForge: [BlockUI](https://www.curseforge.com/minecraft/mc-mods/blockui)
- Discord: [LDTTeam](https://discord.gg/Tb3PagMpaG) · support them on [Patreon](https://www.patreon.com/Minecolonies)

This repository is an **unofficial, community-maintained port to the Fabric loader**. It is not
affiliated with, endorsed by or supported by LDTTeam — please do not send them support requests
about this build.

Licensed under **[GPL-3.0-only](26.2/LICENSE)**, the same license as the upstream project, and
distributed under its terms.
