Players are rewarded for killing monsters with Item that are dropped on the floor on their death, the items dropped are selected at random from a table of options which vary depending on the type monster defeated.

# Tables

Drop tables are defined inside [`.drops.toml`](https://github.com/GregHib/void/blob/main/data/entity/npc/animal/chicken/chicken.drops.toml) files

```toml
[chicken_drop_table]                  # Name of the table
type = "all"                          # How to pick from this table
drops = [                             # List of potential drops
    { table = "chicken_primary" },    # Nested sub table for 100% drops
    { table = "chicken_secondary" },  # Nested sub table for main drops          
    { table = "chicken_tertiary" },   # Nested sub table for extra drops
]


[chicken_primary]           # Sub table for 100% drops
type = "all"                # Drop every item in this sub-table
# Default roll is 1
drops = [
    { id = "raw_chicken" }, # Default 100% chance to drop 1
    { id = "bones" }
]

[chicken_secondary] # Sub-table for main drop
roll = 128          # Roll a random number between 1-128
# Default type "first" stops after one drop
drops = [
    { id = "feather", amount = 5, chance = 64 },  # Drop 5 feathers if roll lands between 1-64 (50% chance)
    { id = "feather", amount = 15, chance = 32 }, # Drop between 10 and 15 feathers if roll lands between 64-96 (25% chance)
]                                                 # Drop nothing if roll lands between 96-128 (25% chance)

[chicken_tertiary]
roll = 300 # Roll between 1-300
drops = [
    { id = "clue_scroll_easy", lacks = "clue_scroll_easy*" }, # Drop only if player doesn't already own an easy clue scroll
    { id = "super_large_egg", variable = "cooks_assistant_egg", equals = false, default = false }, # Check variable is equal or return default if variable isn't set on the player
]
```

## Types
There are two table types by default a tables type will be `first`.
* `all` - drop every item listed
* `first` - drop only one item

### Type: All

Tables with type `all` will drop every item or call every sub-table listed, as such no `chance` parameter is required as all items have a 100% chance of being selected.

```toml
[cow_primary]
type = "all"
drops = [
    { id = "cowhide" },
    { id = "raw_beef" },
    { id = "bones" }
]
```

### Type: First

Most tables however roll a random number between 0 and `roll` and select the first drop in the list that the cumulative `chance` is within.

For example take this table:
```toml
[talisman_drop_table]
roll = 70
drops = [
  { id = "air_talisman", chance = 10 },
  { id = "body_talisman", chance = 10 },
  { id = "earth_talisman", chance = 10 },
  { id = "fire_talisman", chance = 10 },
]
```

* First we roll a random number between 0-70. Let's say 22.
* We check down the list of items starting from the top and a chance of 0
* For `air_talisman` 22 is not within 0-10; so we move on adding 10 to our total.
* For `body_talisman` 22 is not within 10-20; so we move on, adding 10 to our total.
* For `earth_talisman` 2 is within 20-30. So `earth_talisman` is our selected drop.

> [!WARNING]
> Drops chances should never exceed the tables `roll` as that could mistakenly give some drops a 0% chance.


## Nesting

Tables can also be drops themselves allowing for nesting and control over "groups" of drops.

The following example will always drop `bones` and `raw_rat_meat` but `giant_rat_bones` will only have a 25% of being dropped
```toml
[giant_rat_drop_table]
type = "all"
drops = [
    { table = "giant_rat_primary" },
    { table = "giant_rat_secondary" }
]

[giant_rat_primary]
type = "all"
drops = [
    { id = "bones" },
    { id = "raw_rat_meat" }
]

[giant_rat_secondary]
roll = 4
drops = [
    { id = "giant_rat_bone" }
]
```
## Conditionals

### Variables

Items can have optional conditions for being dropped by specifying a [variable](character-variables) and the value it should match

```toml
drops = [
  { id = "blood_rune", amount = 2, chance = 2, members = true }, # Spawn only on members worlds
  { id = "clue_scroll_elite", lacks = "clue_scroll_elite*" }, # Dropped only if the player lacks an item in their inventory, equipment, bank or familiar
  { id = "rats_paper", variable = "what_lies_below_stage", within_min = 2, within_max = 4, default = 0 }, # The range the variable must be within (inclusive)
]
```

### Items

Drops can also have optional conditions if a player owns (has on them or in their bank) or does not own a particular item using the `owns` or `lacks` fields. 

```toml
drops = [
  { id = "small_pouch", chance = 6, lacks = "small_pouch" },   # Only drop if the player doesn't have a small pouch already
  { id = "medium_pouch", chance = 6, charges = 45, owns = "small_pouch", lacks = "medium_pouch*" }, # But not if the player owns any type of medium pouch (i.e. medium_pouch or medium_pouch_damaged)
]
```

# Monster drops

You can specify a drop table for a monster simply by naming a table in either a `<npc_id>_drop_table`, or `<npc_race>_drop_table` format.

The more specific table name will be used first, for example the King Black Dragon will use the `king_black_dragon_drop_table` even though it is classified as one of the dragon race in `*.npcs.tom`, where a Green Dragon will use `dragon_drop_table` as a `green_dragon_drop_table` doesn't exist.

```toml
[green_dragon]
id = 941
categories = ["dragon"]
examine = "Must be related to Elvarg."

[king_black_dragon]
id = 50
categories = ["dragon"]
examine = "One of the biggest, meanest dragons around."
```

# Converting from wiki

Drop tables can be converted from the osrs and rs3 wiki pages (with some [exceptions](#exceptions)) using [DropTableConverter.kt](https://github.com/GregHib/void/blob/main/tools/src/main/kotlin/world/gregs/voidps/tools/convert/DropTableConverter.kt).

Find the drops section of an npcs wiki page, in this example we'll use [Dark wizards](https://oldschool.runescape.wiki/w/Dark_wizard#Level_7_and_11_drops).

Clicking on "edit source" will reveal the underlying [Wikitext](https://en.wikipedia.org/wiki/Help:Wikitext):

![image](https://github.com/GregHib/void/assets/5911414/18ec004a-badb-45b1-a4e2-3a5d7d7ecca5)

Copy and paste the wikitext into and run the DropTableConverter

![image](https://github.com/GregHib/void/assets/5911414/0cf38ad8-b0d9-4468-824f-d63c52211727)

The converter will print out the drop table in TOML with the quanities and rarities converted

```toml
[drop_table]
roll = 128
drops = [
  { id = "nature_rune", amount = 4, chance = 7 },
  { id = "chaos_rune", amount = 5, chance = 6 },
  { id = "mind_rune", amount = 10, chance = 3 },
  { id = "body_rune", amount = 10, chance = 3 },
  { id = "mind_rune", amount = 18, chance = 2 },
  { id = "body_rune", amount = 18, chance = 2 },
  { id = "blood_rune", amount = 2, chance = 2, members = true },
  { id = "cosmic_rune", amount = 2 },
  { id = "law_rune", amount = 3 },
]
```

## Exceptions

> [!CAUTION]
> The converter doesn't verify item ids, so an item named "Wizard hat" will be
> converted to `wizard_hat` even though the `.items.toml` id is `black_wizard_hat`.
> Any incorrect ids will be printed out on server startup: `[ItemDrop] - Invalid drop id wizard_hat`.


> [!WARNING]
> The converter won't correctly convert most [variable conditions](#variables) such as drop limits, dynamic drop rates,
> quest requirements or other exceptions. These will need to be added manually.