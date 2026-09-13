# Groml Config Format

Config files use **Groml** — a TOML subset/variant. Files use `.toml` extension for syntax highlighting. Full spec: [config/README.md](../config/README.md).

## Differences from TOML

- **No** tables (`[[...]]`), dates, `inf`/`nan`, hex/octal/binary integers, or multi-line strings.
- **Section inheritance** — `.`-prefixed header inherits the previous base name: `[.foo]` inside `[bar]` becomes `bar.foo`.
- **Clone** — `clone = "other_section"` copies all values from that section; keys defined after override.

## Example

```toml
[antique_lamp_easy]
id = 11137
examine = "I wonder what happens if I rub it."

[antique_lamp_medium]
clone = "antique_lamp_easy"
id = 11139   # overrides cloned value
```
