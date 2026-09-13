# Groml Config Format

Config files use **Groml** — a TOML subset/variant. Files use `.toml` extension for syntax highlighting. Full spec: [config/README.md](../config/README.md).

> [!NOTE]
> Groml is deliberately smaller than TOML — it exists for fast, readable content data, not as a
> general-purpose config format. If you need arrays or nested tables, that's a sign the data
> belongs in a different structure.

## Differences from TOML

- **No** tables (`[[...]]`), dates, `inf`/`nan`, hex/octal/binary integers, or multi-line strings.
- **Section inheritance** — `.`-prefixed header inherits the previous base name: `[.foo]` inside `[bar]` becomes `bar.foo`.
- **Clone** — `clone = "other_section"` copies all values from that section; keys defined after override.

> [!WARNING]
> Clone order matters — `clone` only copies values already defined in the source section at parse
> time. Cloning a section declared later in the file copies nothing.

## Example

```toml
[antique_lamp_easy]
id = 11137
examine = "I wonder what happens if I rub it."

[antique_lamp_medium]
clone = "antique_lamp_easy"
id = 11139   # overrides cloned value
```

```json
{
  "antique_lamp_easy": {
    "id": 11137,
    "examine": "I wonder what happens if I rub it."
  },
  "antique_lamp_medium": {
    "id": 11139,
    "examine": "I wonder what happens if I rub it."
  }
}
```
