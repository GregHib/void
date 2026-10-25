# Code Style

## Structural Patterns

- **Early exits** — guard disqualifying conditions first; no nested `if` pyramids. No one-line guards or for loops. Braces on every block.
- **Exhaustive `when`** over if/else chains on state.
- **Handler order** — guards → `val` setup → mutation → check result → effects (anim/gfx/sound/xp/audit) last.
- **Use the receiver** — call `message(...)`, `inventory`, etc. bare; no `player.` re-qualification.
- **`val` by default**; snake_case string IDs; class name matches file; no `by lazy {}`.
- **Comments** — infrequent; only when code can't self-document.
- Format with `gradle spotlessApply --parallel`

## Content & Scripts

Applies to anything under `game/.../content` (scripts, dialogue, quests, skills, areas, etc.).

- **Thin scripts** — `init {}` only registers handlers; logic goes in `private` methods on the script class.
- **Split content into focused script classes** — never put a whole quest/area/skill in one script class. Group by piece of content: each NPC gets its own class (interactions + dialogue), doors/gates for an area get their own, objects, items, and so on each get their own file. A quest is a package of many small scripts, not one large one. Shared pieces go in a `companion object` (see below).
- **Simple item combinations are recipes, not scripts** — don't write `itemOnItem(...) { }` handlers for basic "use X on Y → remove/add items, message" interactions. Declare them in a `*.recipes.toml` (e.g. `data/area/kandarin/catherby/catherby.recipes.toml`: `remove`, `add`, `message`). Only use a script when the interaction needs real logic (requirements, dialogue, branching).
- **Blank line between calls in `init {}`** — separate each handler registration/method call with an empty line.
- **Keep methods inside the script class** — every helper (including extensions like `private fun Player.x()` or `private suspend fun ChoiceOption.y()`) is a member of the script class that uses it, never a top-level declaration after the class.
  - Helpers shared between script classes of the *same* content (e.g. several files of one quest or skill) go in a `companion object` and are called from the other scripts.
  - Top-level functions are only for helpers shared broadly across multiple *different* pieces of content (roughly 5+ unrelated users).
  - A `private` top-level function in a content file is always wrong.
- **String IDs are literals, not constants** — write `"penguin_points"`, `"hive_420"` inline at the call site. Don't declare `const val` (or `val`) for item/npc/object/variable/timer ids, even when repeated. `const` is only for non-id values (numbers, tick counts) and belongs in the `companion object`.
- **No static `object` classes for content** — avoid `object Foo { ... }` holding content logic or state; use the script class and its `companion object`.
- **No data classes for data** — don't create new definitions or data classes, and don't copy rows into them. Store data in `*.tables.toml` and read it from `Tables`/`Rows` (see below).
- **Store state on entities, not in scripts** — per-player/npc/object state goes in entity variables/clocks/timers (declared in the `.vars.toml` configs), not in script fields or maps keyed by entity. Script-level state is only for genuinely global events, e.g. distractions and diversions (Evil Tree, Shooting Star, etc.).
- **Don't change the engine** — the engine is feature complete. Content should not need engine changes unless absolutely critical; if it seems to, you're most likely approaching the problem the wrong way. Look for how existing content solves it, or ask.
- Use wildcards and comma-separated IDs over multiple handler registrations `npcApproach("Spy-on", "*_penguin,*_turkey") { ... }`.
- Persistent variables must be declared in `.vars.toml`, `.varbits.toml`, or `varps.toml` configs.
- **Citations** — comment data with its origin: wiki/web data gets the URL, a made-up `message()` gets `// TODO proper message`, a guessed stat or value is commented as guessed (also in `.toml`).

### Data

Data lives in `*.tables.toml` structured around access.

- Header declares column types.
- Single value accessed as `Tables.int("spells.$spell.xp")`.
- Rows accessed as `val row = Rows.get("cooking.${itemId}")`.
- All rows as `Tables.get("barrows_brothers").rows()`.
- Fields as `row.int("level")`, `row.string("type")`, etc.
- Read fields from the `Row` directly, and search `Tables.get(...).rows()` when needed.
