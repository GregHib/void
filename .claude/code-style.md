# Code Style

## Structural Patterns

- **Early exits** — guard disqualifying conditions first; no nested `if` pyramids. No one-line guards or for loops. Braces on every block.
- **Thin scripts** — `init {}` only registers handlers; logic goes in `private` helpers or `suspend fun Player.x()` extensions.
- **Small global scope** — top-level functions only if many unrelated script usages. Content-specific helpers go in `companion object`'s.
- **Exhaustive `when`** over if/else chains on state.
- **Handler order** — guards → `val` setup → mutation → check result → effects (anim/gfx/sound/xp/audit) last.
- **Use the receiver** — call `message(...)`, `inventory`, etc. bare; no `player.` re-qualification.
- **`val` by default**; snake_case string IDs; class name matches file; no `by lazy {}`.
- **Comments** — infrequent; only when code can't self-document.
- Use wildcards and comma-separated IDs over multiple handler registrations `npcApproach("Spy-on", "*_penguin,*_turkey") { ... }`.
- Data lives in `*.tables.toml` structured around access.
  - Header declares column types.
  - Single value accessed as `Tables.int("spells.$spell.xp")`.
  - Rows accessed as `val row = Rows.get("cooking.${itemId}")`.
  - All rows as `Tables.get("barrows_brothers").rows()`.
  - Fields as `row.int("level")`, `row.string("type")`, etc.
  - **No data classes for table data** — don't copy rows into them; read fields from the `Row` directly, and search `Tables.get(...).rows()` when needed.
- Persistent variables must be declared in `.vars.toml`, `.varbits.toml`, or `varps.toml` configs.
- Format with `gradle spotlessApply --parallel`
- **Citations** — comment data with its origin: wiki/web data gets the URL, a made-up `message()` gets `// TODO proper message`, a guessed stat is commented as guessed (also in `.toml`).

