# Code Style

## Structural Patterns

- **Early exits** — guard disqualifying conditions first; no nested `if` pyramids, one-line guards or for loops. Braces on every block.
- **Exhaustive `when`** over if/else chains on state.
- **Handler order** — guards → `val` setup → mutation → check result → effects (anim/gfx/sound/xp/audit) last.
- **Use the receiver** — call `message(...)`, `inventory`, etc. bare; no `player.`.
- **`val` by default**; snake_case string IDs; class name matches file; no `by lazy {}`.
- **Comments** — infrequent; only when code can't self-document.
- Format with `gradle spotlessApply --parallel`

## Content & Scripts

Applies to anything under `game/.../content` (scripts, dialogue, quests, skills, areas, etc.).

- **Thin scripts** — `init {}` only registers handlers; logic goes in `private` methods on the script class.
- **Split content into focused script classes** — never put a whole quest/area/skill in one class. Each NPC gets its own class (interactions + dialogue); doors/gates of an area, objects, items etc. each get their own file. A quest is a package of many small scripts.
- **Simple item combinations are recipes** — no `itemOnItem(...) { }` for basic "use X on Y → remove/add items, message". Use a `*.recipes.toml` (e.g. `data/area/kandarin/catherby/catherby.recipes.toml`: `remove`, `add`, `message`). Scripts only when real logic is needed (requirements, dialogue, branching).
- **Blank line between calls in `init {}`**.
- **Methods stay in the script class** — every helper (including extensions like `private fun Player.x()`) is a member of the class that uses it, never top-level.
  - Helpers shared between scripts of the *same* content (e.g. one quest or skill) go in a `companion object`.
  - Top-level functions only for helpers shared across many *different* content (roughly 5+ unrelated users).
  - A `private` top-level function in a content file is always wrong.
- **String IDs are literals** — write `"penguin_points"` inline; no `const val`/`val` for item/npc/object/variable/timer ids, even when repeated. `const` is only for non-id values (numbers, tick counts), in the `companion object`.
- **No static `object` classes for content** — use the script class and its `companion object`.
- **No data classes for data** — don't create new definitions or data classes, or copy rows into them. Store data in `*.tables.toml`, read via `Tables`/`Rows` (see below).
- **State lives on entities** — per-player/npc/object state goes in entity variables/clocks/timers (declared in `.vars.toml` configs), not script fields or entity-keyed maps. Script-level state only for global events, e.g. distractions and diversions (Evil Tree, Shooting Star).
- **Don't change the engine** — it's feature complete. Engine changes only if absolutely critical; otherwise you're likely approaching the problem wrongly. Look at how existing content does it, or ask.
- Prefer wildcards and comma-separated IDs over multiple registrations: `npcApproach("Spy-on", "*_penguin,*_turkey") { ... }`.
- Persistent variables must be declared in `.vars.toml`, `.varbits.toml`, or `varps.toml`.
- **Citations** — comment data origin: wiki/web gets the URL, a made-up `message()` gets `// TODO proper message`, guessed values are commented as guessed (also in `.toml`).

### Data

`*.tables.toml` structured around access; header declares column types. Always access inline.

- Single value: `Tables.int("spells.$spell.xp")`
- Row: `val row = Rows.get("cooking.${itemId}")`
- All rows: `Tables.get("barrows_brothers").rows()` (search these when needed)
- Fields: `row.int("level")`, `row.string("type")`, etc. — read from the `Row` directly.
