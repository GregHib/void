# CLAUDE.md

**Void** is a 2011 RuneScape private server emulator (revision 634) written in Kotlin. JVM 21+, Gradle 9. Main artifact: `void-server.jar`.

## Build Commands

```bash
./gradlew build -x test        # initial setup (cache files needed for tests)
./gradlew test                 # all tests
./gradlew test --tests "content.skill.firemaking.FiremakingTest"
./gradlew :game:test           # single module
./gradlew spotlessApply        # format (required before committing)
./gradlew shadowJar            # distributable JAR
./gradlew run                  # start server (working dir = root)
./gradlew bundleDistribution   # JAR + data + startup scripts
```

Tests require cache files in `data/cache/`. CI runs engine unit tests on all PRs; full content tests only on main.

## Modules

Dependency direction: `game` → `engine`, `cache`, `network`, `config`, `types`.

- **engine** — Game loop, entity model (Player, NPC, GameObject, FloorItem), event system, inventory transactions, pathfinding, all `Script` mix-in interfaces.
- **game** — All content (`content/`), `Main.kt`, `WorldTest.kt`.
- **cache** — Decodes binary RS cache files via Displee.
- **config** — YAML overrides layered on cache data (string IDs, shops, drop tables, etc.).
- **network** — Ktor server for connections, login, packet encoding/decoding.
- **buffer** — Binary read/write utilities.
- **types** — Shared value types (`Tile`, `Item`, `Direction`, etc.).
- **database** — Optional PostgreSQL via Exposed ORM (`-PincludeDb` only).
- **tools / tools:app** — Dev utilities and Compose cache viewer. Not part of the server.

## Architecture

**Content Scripts** — Kotlin classes implementing `Script` (`engine/.../Script.kt`). The interface composes many mix-ins (`VariableApi`, `InventoryApi`, `CombatApi`, `Dialogues`, etc.) so content calls engine APIs directly. Handlers register in `init {}`. `ScriptMetadataTask` generates `scripts.txt` at build time; `ContentLoader` instantiates all scripts via reflection at startup.

Content layout: `content/{achievement,activity,area,entity,item,quest,skill,social}/`

**Events** — Network decodes clicks into `ClientInstruction` events; scripts subscribe to them. Routing uses a YAML-loaded `Wildcards` system.

**DI (Koin)** — Singletons registered in `Main.kt`/`EngineModules.kt`, retrieved with `get<T>()`. Key singletons: `ItemDefinitions`, `NPCDefinitions`, `ObjectDefinitions`, `Players`, `NPCs`, `GameObjects`, `FloorItems`.

**Game Loop** — `GameLoop` ticks every 600 ms through ordered `Runnable` stages (NPC movement, player updates, packet processing, item respawns).

**Inventory Transactions** — All mutations go through `engine/.../inv/transact/`. Operations staged and committed atomically; failures roll back.

## Testing

Content tests extend `WorldTest` (`game/src/test/kotlin/WorldTest.kt`): boots full Koin context, loads all definitions, instantiates all scripts, resets world state between tests. Helpers: `createPlayer()`, `createNPC()`, `createObject()`, `createFloorItem()`, `tick()`, `tickIf()`.

`WorldTest` is expensive (`@TestInstance(PER_CLASS)`). Use only for integration-level content tests. Engine unit tests use plain JUnit 5.

## Code Style

ktlint (`intellij_idea` style) via Spotless. Wildcard imports allowed. Structural patterns in brief:
- **Early exits**: guard every disqualifying condition first with `return`/`return@label`; no nested `if` pyramids. Braces on every block.
- **Thin scripts**: `init {}` only registers handlers; logic goes in `private` helpers or `suspend fun Player.x()` extensions.
- **Exhaustive `when`** over if/else chains on state (quest stage, `TransactionError`).
- **Handler order**: guards → `val` setup → mutation → check result → effects (anim/gfx/sound/xp/audit) last, only after success.
- **Use the receiver**: call `message(...)`, `inventory`, etc. bare; no `player.` re-qualification.
- **`val` by default**; snake_case string IDs matching config; class name matches file; comments rare.
