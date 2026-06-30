# Code Style

ktlint (`intellij_idea` style) via Spotless. Wildcard imports allowed.

## Structural Patterns

- **Early exits** — guard every disqualifying condition first with `return`/`return@label`; no nested `if` pyramids. Braces on every block.
- **Thin scripts** — `init {}` only registers handlers; logic goes in `private` helpers or `suspend fun Player.x()` extensions.
- **Exhaustive `when`** over if/else chains on state (quest stage, `TransactionError`).
- **Handler order** — guards → `val` setup → mutation → check result → effects (anim/gfx/sound/xp/audit) last, only after success.
- **Use the receiver** — call `message(...)`, `inventory`, etc. bare; no `player.` re-qualification.
- **`val` by default**; snake_case string IDs matching config; class name matches file; comments rare.
