As Void can have a number of bot controlled [players](players) to make the world feel more alive and active even when playing offline and locally.

<img width="767" height="535" alt="Screenshot 2026-02-17 230947" src="https://github.com/user-attachments/assets/0ccbd161-a456-4fa7-99cc-d4b2d5ce0257" />

You can customise the number of bots spawned in your world in [Game Settings](game-settings#bots).

Bot configuration is split into 5 parts total; the most important two being `*.bots.toml` and `*.setups.toml`, with `*.templates.toml` shared across them. The remaining two `*.nav-edges.toml` and `*.shortcuts.toml` are used for bot navigation only.

See the [bot-architecture](bot-architecture) page for more details on how the system works.

# Activities

Activities are things a bot can do, what requirements it needs, and where it needs to be to do it.

```toml
[cut_trees]
# Hard Requirements - What a bot must have to be assigned this activity
requires = [
    { skill = { id = "woodcutting", min = 1, max = 15 } },
]
# Things it might not have right now but will need to be setup before starting
setup = [
    { inventory = [{ id = "steel_hatchet,iron_hatchet" }, { id = "empty", min = 27 }] },
    { area = { id = "lumbridge_trees" } },
]
# The action(s) to carry out to do the activity
actions = [
    { object = { option = "Chop down", id = "tree*", delay = 5, success = { inventory = { id = "empty", max = 0 } } } },
]
# Things the activity produces (used for resolver matching and checking if a bot has timed-out)
produces = [
    { item = "logs" },
    { skill = "woodcutting" }
]
```

# Gear & Loadouts

Gear and loadouts decide what a bot wears and wields. Gear lives in `*.gear.toml` and loadouts live in `*.loadouts.toml`.

## Gear

A gear list is a list of interchangeable items for one equipment slot. Items are ordered best to worst. A bot takes the best item it can equip and owns.

```toml
# melee.gear.toml
[melee_weapons]
items = [
    "rune_scimitar",
    "rune_longsword",
    "adamant_scimitar",
    "bronze_sword",
]
```

## Loadouts

A loadout is a multi-slot gear setup.

Each slot takes either `gear = "<gear id>"` or `id = "<item ids>"`. Slots also take `min`, `max` and `optional`. An `optional` slot never blocks an activity.

A loadout can also have `inventory = [...]`, `autocast` and `weight`.

Inventory entries can reference gear too, for tools carried rather than worn. These pick the best item the bot has the level to *use* (e.g. mining level for a pickaxe), not the level to wield it.

```toml
# tools.loadouts.toml
[mining]
inventory = [{ gear = "pickaxes", usable = true }]
```

```toml
# melee.loadouts.toml
[melee]
weapon = { gear = "melee_weapons" }
hat = { gear = "melee_helms", optional = true }
chest = { gear = "melee_bodies", optional = true }
legs = { gear = "melee_legs", optional = true }
shield = { gear = "melee_shields", optional = true }
```

## Using a loadout in an activity

Add a loadout row to the activity's `setup`.

```toml
[kill_cows]
setup = [
    { loadout = { id = "melee" } },
    { area = { id = "$location" } },
    { inventory = { id = "empty", min = 27 } },
]
```

To pick between loadouts, give a list. Each entry's `weight` is its chance. If an entry has no weight, it uses the loadout's own `weight`, which defaults to 1.

```toml
setup = [
    { loadout = [{ id = "a", weight = 3 }, { id = "b" }] },
]
```

- Loadouts only work in activity `setup`. They can't be used in resolvers or shortcuts.
- An activity can have only one loadout row.

## How a bot picks its gear

A bot picks its gear once, when the activity is assigned.

1. Pick a qualifying loadout by weight.
2. For each gear slot, use the best item the bot can equip and already owns.
3. If it owns none, a required slot uses the best item it can get from a shop or setup resolver. Otherwise, it uses the best item it can equip.
4. Optional slots only use items the bot already owns.
5. A two-handed weapon removes the shield. A required shield rules out two-handed weapons.
6. Explicit `id` slots are used as written.
7. The loadout's `inventory` is merged into the activity's inventory requirement. Set `empty` counts so they leave room for it. Inventory gear entries are picked like gear slots, using the level to use the item.
8. Ammo compatibility is not checked. Use explicit `id` slots for ammo.

## Requirements

An activity with a loadout is only available to bots with the levels to equip at least one item in each required gear slot. This requirement is added automatically.

## Spawned bots

With `bots.spawnRequirements`, a bot gets the levels for the loadout. It also gets the best item it can equip for every slot, including optional ones.

# Allocations

An allocation decides how many bots spawn into a part of the world, and which role each one gets. Allocations are rows in the `bot_allocations` table, in `data/bot/bot_allocation.tables.toml`.

```toml
[bot_allocations]
[.world]
count_setting = "bots.count"
spawn_seconds_setting = "bots.spawnSeconds"
spawn_areas = ["lumbridge_teleport", "varrock_teleport", "draynor_bank", "camelot_teleport"]
saved = true
starter_items = [["coins", 10000]]
```

- `row_id`: unique id, used by `::bots`.
- `mode`: `capacity` (default) or `weighted`. The `world` row leaves it out.
- `count` / `count_setting`: how many bots the allocation wants. The setting overrides `count` when set.
- `spawn_seconds` / `spawn_seconds_setting`: seconds between spawns, default 60. The setting overrides it when set.
- `spawn_areas`: areas bots spawn in, picked at random.
- `refill`, `saved`, `starter_items`: see Spawning below.
- `roles`: `bot_roles` rows, weighted mode only.

## Modes

- `capacity` is how world bots work. Bots pick any available activity, limited by each area's `bot_capacity`.
- `weighted` gives each bot a fixed role from `roles`, by weight. Role minimums are filled first. The rest is split by weight (largest remainder) and capped at each role's `max`. Roles are fixed for the bot's whole life.

The Clan Wars free-for-all rows, `clan_wars_ffa_safe` and `clan_wars_ffa_dangerous`, are the real weighted allocations. Trimmed excerpt:

```toml
[bot_allocations]
[.clan_wars_ffa_safe]
mode = "weighted"
count_setting = "bots.combat.clan_wars_ffa_safe.count"
spawn_seconds = 10
spawn_seconds_setting = "bots.combat.clan_wars_ffa_safe.spawnSeconds"
spawn_areas = ["clan_wars_teleport"]
refill = true
starter_items = [["coins", 10000]]
roles = [
    "bot_roles.clan_wars_safe_zerker",
    "bot_roles.clan_wars_safe_dharoker",
    # ...8 roles in total
]

[bot_roles]
[.clan_wars_safe_zerker]
activity = "clan_wars_ffa_safe_zerker"
builds = [["bot_builds.clan_wars_safe_zerker", 1]]
combat_style = "slash"
```

`clan_wars_ffa_dangerous` has 4 roles. Each role has one fixed-level build. With only `weight` set, roles are split evenly. Using `min` and `max` on a role changes the split.

Role activities use templates from `data/bot/minigame_combat.templates.toml`. Their kit is a PvP loadout (e.g. `pvp_zerker`) from `data/bot/pvp.loadouts.toml`. Slots with several options, such as torva or bandos for `ags_main`, use gear lists from `data/bot/pvp.gear.toml`.

Bots follow the same death rules as players, so respawn tiles and item drops come from the minigame's own content (e.g. `ClanWarsFreeForAll`). `restock_areas` lists areas which re-provision a role bot when it enters them. `clan_wars_ffa_dangerous` sets `restock_areas = ["clan_wars_teleport"]` so bots that retreat are restocked before walking back in.

## Spawning

Each allocation has its own spawn timer. While it is below its target, it spawns one bot every `spawn_seconds`.

- `refill = false` (default): stop once `count` bots have been spawned. Bots that leave are not replaced, so `::clear_bots` stays clear.
- `refill = true`: keep topping up to `count` as bots leave.
- `saved = true`: may reuse saved bot accounts, with the chance set by `bots.spawnSavedChance`.
- `starter_items`: given to brand-new capacity bots. Role bots get them every time they are provisioned.

## Roles and builds

A role (`bot_roles`) has:
- `activity`: what its bots do.
- `weight` (default 1), `min` (default 0) and an optional `max`.
- `builds`: weighted `bot_builds` rows. A role with no builds keeps its existing levels.
- `combat_style`: optional.

A build (`bot_builds`) has a level range `[min, max]` for each skill. Each skill is rolled within its range. Skills not listed are left alone. Constitution is given in levels, not x10.

## Provisioning

A role bot is provisioned when it spawns.

1. A build is picked by weight. Its levels are rolled once and kept.
2. The role activity's loadout is picked, raising levels if it needs to. It is remembered as the bot's loadout, so binding later picks the same one.
3. Items are cleared, the starter items are added and the loadout is worn.
4. Explicit equipment and inventory setup rows are applied. Hybrid loadout extras not being worn are added to the inventory.

On death, a role bot respawns with the same levels and kit.

## Command

`::bots [count] [allocation]` spawns `count` bots (default 1) into `world` by default. Allocation ids are autofilled. For example `::bots 14 clan_wars_ffa_safe` spawns 14 safe Clan Wars bots, or use `clan_wars_ffa_dangerous` for the dangerous arena.

# Resolvers

Resolvers are activities bots can complete to get stuff which is needed for another activity.
E.g. if a bot needs air and mind runes to cast spells on a combat dummy it will need to talk with the magic tutor to get them.

```toml
[magic_tutor_runes]
requires = [
    { clock = { id = "claimed_tutor_consumables", max = 0, seconds = true } },
    { bank = [
        { id = "air_rune", max = 0 },
        { id = "mind_rune", max = 0 },
    ] },
    { area = { id = "lumbridge_combat_tutors" } }
]
actions = [
    # There are lots of different types of actions most types of interactions
    { npc = { option = "Talk-to", id = "mikasi", delay = 5, success = { interface_open = { id = "dialogue_npc_chat3" } } } },
    { continue = { id = "dialogue_npc_chat3:continue", success = { interface_open = { id = "dialogue_multi4" } } } },
    { continue = { id = "dialogue_multi4:line3", success = { interface_open = { id = "dialogue_obj_box" } } } },
    { continue = { id = "dialogue_obj_box:continue", success = { inventory = { id = "air_rune", min = 30 } } } },
    { continue = { id = "dialogue_obj_box:continue", success = { inventory = { id = "mind_rune", min = 30 } } } },
]
produces = [
    { item = "air_rune" }, # Produces is how setups figure out what resolvers can solve their requirement.
    { item = "mind_rune" }
]
```

## Templates

Templates are re-usable activities where you can put placeholder `$fields` which can be replaced later.

For example a template:

```toml
[cooking_template]
requires = [
    { skill = { id = "cooking", min = "$level" } },
]
```

Can be used like this:

```toml
[cooking_trout]
template = "cooking_template"
fields = { level = 15 }
```

And is equivalent to:

```toml
[cooking_trout]
requires = [
    { skill = { id = "cooking", min = 15  } },
]
```

You can have multiple fields for any kind of value, anywhere in an activity including in the middle of a string.
Templates can be shared between activities, resolvers and shortcuts, but templates can't reference each other.

```toml
[cooking_template]
requires = [
    { bank = { id = "$raw", amount = 28 } },
    { skill = { id = "cooking", min = "$level" } },
]
setup = [
    { inventory = { id = "$raw", min = 28 } },
    { area = { id = "$location" } },
]
actions = [
    { item_on_object = { id = "$raw", object = "$obj", success = { interface_open = { id = "dialogue_skill_creation" } } } },
    { interface = { option = "All", id = "skill_creation_amount:all" } },
    { continue = { id = "dialogue_skill_creation:choice1" } },
    { restart = { wait_if = { queue = { id = "cooking" } }, success = { inventory = { id = "$raw", max = 0 } } } }
]
produces = [
    { skill = "cooking" },
    { item = "$cooked" },
    { item = "$burnt" }
]
```

