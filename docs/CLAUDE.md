# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

**Void** is a 2011 RuneScape private server emulator written in Kotlin. It emulates revision 634 of the game, runs on JVM 21+, and is built with Gradle 9. The main artifact is `void-server.jar` (or `void-server-db.jar` with PostgreSQL support).

## Build Commands

```bash
# Initial setup (skip tests until cache files are in /data/cache/)
./gradlew build -x test

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "content.skill.firemaking.FiremakingTest"

# Run tests in a specific module
./gradlew :game:test
./gradlew :engine:test

# Format code (required before committing)
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck

# Build the distributable JAR
./gradlew shadowJar

# Build with PostgreSQL support
./gradlew shadowJar -PincludeDb

# Start the server (working dir is root)
./gradlew run

# Create a release bundle (JAR + data files + startup scripts)
./gradlew bundleDistribution
```

Tests require game cache files extracted into `data/cache/`. Without them, tests that load cache data will fail. CI runs engine unit tests (no cache needed) on all PRs, and full content tests only on main.

## Module Structure

The project is a multi-module Gradle build. Dependency direction: `game` → `engine`, `cache`, `network`, `config`, `types`.

- **engine** — Core game loop (`GameLoop`), entity model (Player, NPC, GameObject, FloorItem), event system, inventory transactions, collision, timers, pathfinding, and all the `Script` interface mix-ins that content scripts use.
- **game** — All content: quests, skills, activities, NPC behaviours, area logic. Everything under `game/src/main/kotlin/content/`. Also contains `Main.kt` (entry point) and `WorldTest.kt` (test base class).
- **cache** — Reads and decodes the RuneScape binary cache files (items, NPCs, objects, animations, interfaces, etc.) using the Displee library.
- **config** — Loads YAML definition overrides that layer on top of cache data (item/NPC/object string IDs, custom properties, shops, inventories, drop tables, etc.).
- **network** — Ktor-based server for player connections, login, and client instruction decoding/encoding.
- **buffer** — Binary read/write utilities used by network and cache.
- **types** — Shared value types (`Tile`, `Item`, `Direction`, etc.) used across all modules.
- **database** — Optional PostgreSQL persistence via Exposed ORM. Only compiled when `-PincludeDb` is passed.
- **tools / tools:app** — Offline developer utilities and a Compose Desktop cache viewer. Not part of the server.

## Architecture

### Content Scripts

All game content is written as Kotlin classes implementing the `Script` interface (`engine/src/main/kotlin/world/gregs/voidps/engine/Script.kt`). The `Script` interface is a composition of many mix-in interfaces (`VariableApi`, `InventoryApi`, `TimerApi`, `InterfaceApi`, `CombatApi`, `Dialogues`, etc.) so content code can call any engine API directly.

Scripts register event handlers at class-load time (inside `init {}` blocks or at the top level). The `ScriptMetadataTask` Gradle task scans `game/src/main/kotlin/content/` at build time, generates `scripts.txt`, and `ContentLoader` uses that list to instantiate every script via reflection at server start.

Content is organised by domain:
```
content/
├── achievement/       Task system, lamps, explorer's ring
├── activity/          Minigames and world events (shooting star, etc.)
├── area/              Region-specific logic grouped by real-world geography
│   └── misthalin/lumbridge/...
├── entity/            Interactions: NPCs, objects, player effects, combat
├── item/              Item-use and equipment scripts
├── quest/             free/ and member/ quests
├── skill/             One subdirectory per skill
└── social/            Friends, clan, trade, grand exchange, ignore
```

### Event / Interaction System

Interactions (click NPC option 1, use item on object, etc.) are decoded by the network layer into `ClientInstruction` objects, which are dispatched as typed events. Content scripts subscribe to these events by registering handlers inside their class bodies. The engine uses a `Wildcards` system for pattern-matched event routing (loaded from YAML, not hardcoded).

### Dependency Injection (Koin)

All singletons — definitions, world state, managers — are registered with Koin in `Main.kt` (and `EngineModules.kt`). Scripts and tests retrieve them with `get<T>()`. The key singletons are the definition objects (`ItemDefinitions`, `NPCDefinitions`, `ObjectDefinitions`, etc.) and `World`, `Players`, `NPCs`, `GameObjects`, `FloorItems`.

### Game Loop

`GameLoop` runs at 600 ms per tick. Each tick executes an ordered list of `Runnable` stages returned by `getTickStages(...)`. The stages handle NPC movement, player updates, client packet processing, item respawns, etc.

### Inventory Transactions

All inventory mutations go through a transactional API (`engine/src/main/kotlin/world/gregs/voidps/engine/inv/transact/`). Operations (`add`, `remove`, `move`, `swap`, etc.) are staged and committed atomically. Failures roll back cleanly.

## Testing

All content tests extend `WorldTest` (`game/src/test/kotlin/WorldTest.kt`), which:
- Boots a full Koin context with all definitions loaded from `data/`
- Instantiates all content scripts via `ContentLoader`
- Provides helpers: `createPlayer()`, `createNPC()`, `createObject()`, `createFloorItem()`, `tick()`, `tickIf()`
- Resets world state (`Players`, `NPCs`, `FloorItems`, `GameObjects`) between each test
- Uses `emptyTile = Tile(2655, 4640)` as the default safe spawn tile

Because `WorldTest` loads all config files once per class (`@TestInstance(PER_CLASS)`), it is expensive. Use it only for integration-level content tests. Engine unit tests (`engine/src/test/`) do not extend it and use plain JUnit 5 + Mockk.

Test heap is set to 5 GB in `buildSrc/src/main/kotlin/shared.gradle.kts`. Tests stop on first failure (`failFast = true`).

## Code Style

- ktlint with `intellij_idea` style, enforced by Spotless. Always run `./gradlew spotlessApply` before committing.
- Wildcard imports are allowed (`ktlint_standard_no-wildcard-imports` is disabled).
- Kotlin compiler flags in use: `-Xinline-classes`, `-Xcontext-parameters`, `-Xjvm-default=all-compatibility`.

### Structural conventions

Beyond the linter, content code follows consistent structural patterns. See `docs/CODE_STYLE_FOR_LLMS.md` for the full guide with examples. In brief:

- **Flatten with early exits.** Guard every disqualifying condition first with `return`/`return@label` (no nested `if` pyramids). Braces on every block.
- **Scripts are thin shells.** `init {}` only registers handlers; multi-step logic goes in `private` helpers or `suspend fun Player.x()` extensions.
- **Exhaustive `when` over if/else chains** when branching on state (quest stage, `TransactionError`); handle every arm explicitly.
- **Handler order:** guards → local `val` setup → state mutation → check result → feedback effects (anim/gfx/sound/xp/audit) last, only after success.
- **Use the receiver** — call `message(...)`, `inventory`, etc. bare; don't re-qualify with `player.`.
- **`val` by default**, `var` only for real loop state. Lowercase snake_case string ids matching config; class name matches file. Comments are rare and purposeful.