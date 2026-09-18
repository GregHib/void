
Tables store content data of most shapes and sizes in `*.tables.toml` files.

They function similar to a spreadsheet storing data in rows and columns.

## Example
### Fletching darts
| Row (item) | Level (int) | XP (int) |
| ---------- | ----------- | -------- |
| bronze_dart_tip | 10 | 18 |
| iron_dart_tip | 22 | 38 |
| steel_dart_tip | 37 | 75 |

## Format
The top of a table lists the column types
```toml
[fletching_darts] # Table name
row_id = "item"   # Row type (optional)
level = "int"     # column 1
level_default = 1 # default value
xp = "int"        # column 2
```

Rows can then be added to that table in the same file using the `.` prefix to refer to the parent
```
[.bronze_dart_tip] # First row
level = 10
xp = 18

[.iron_dart_tip] # Second row
level = 22
xp = 18

[.steel_dart_tip] # Third row
level = 37
xp = 38
```

> [!NOTE]
> Row ids can be written out in full i.e. `[fletching_darts.black_dart_tip]`

## Reading

Tables can be read by value:

```kotlin
val level = Tables.int("fletching_darts.iron_dart_tip.level") // 22
```

Or by row:

```kotlin
val dart = Rows.get("fletching_darts.iron_dart_tip")

val level = dart.int("level")
val xp = dart.int("xp")
```

## Defaults

Each columns type has a default value

```
int = 0
string = ""
list = []
boolean = false
```

These values can be overridden in the table header
```toml
[picking]
item = "item"
message = "string"
chance = "int"
chance_default = 1
respawn = "int"

[.cabbages]
item = "cabbage"
respawn = 45
message = "You pick a cabbage."
```

```kotlin
val chance = Tables.int("picking.cabbages.chance") // 1
val respawn = Tables.int("picking.cabbages.respawn") // 0
```