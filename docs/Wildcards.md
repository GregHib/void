Wildcards let you target multiple IDs at once when writing [scripts](scripts).

One string can match one or many IDs depending on the pattern, multiple patterns can be combined at once.

## No wildcards

Matches exactly one thing
 - `"cow"`
 - `"bank"`
 - `"doric"`

## Match any - `*`

Matches any sequence of characters

- `"cow_*"` // cow_1, cow_big, cow_brown
- `"banker*"` // banker_1, banker_draynor, banker_varrock
- `"attack_style_*"`

## Match all

`"*"` Matches any value of any kind.

## Match digit - `#`

Matches one digit.

- `"cow_#"` // cow_1, cow_2, cow_7
- `"goblin_#"` // cow_0, ... cow_9

## Lists - `,`
Useful when matching a few unrelated IDs:
- `cow,bull,sheep`
- `bronze_arrows,iron_darts,steel_knives`


# Where You'll Use Wildcards

Wildcards are used anywhere an ID is accepted in [event handlers](event-handlers).

```kotlin
npcSpawn("cow_*") { }
objectOperate("Open", "*_chest") { }
itemOnNPCOperate("bucket,bucket_of_milk", "dairy_cow") { }
interfaceOption("*", "worn_equipment:*_slot") { }
```

> [!NOTE]
> For a more technical understand of how wildcards work see [Wildcard System](wildcard-system).