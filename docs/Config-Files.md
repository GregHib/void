Config files are `.toml` files stored in /data/ directory that can store pretty much any data - often related to a specific entity and only used to define [string ids](string-identifiers).

Below is a list of configuration types, a description and example.

## Areas
Bounding boxes for areas that trigger [entered/exited](https://github.com/GregHib/void/blob/9c82412c374ced55dbcce337e34de8815e7bb9d2/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/mode/move/Moved.kt#L26)

[ardougne.areas.toml](https://github.com/GregHib/void/blob/main/data/area/kandarin/ardougne/ardougne.areas.toml)

```toml
[ardougne_teleport]
x = [2660, 2664]
y = [3304, 3308]
tags = ["teleport"]
```
<img width="386" height="329" alt="image" src="https://github.com/user-attachments/assets/a587f606-8f61-4128-9737-fef1bfe2fb3c" />

[Map link](https://greghib.github.io/void-map/?centreX=2662&centreY=3294&centreZ=0&zoom=10)

## Sounds
Sound definition extras

[flesh_crawler.sounds.toml](https://github.com/GregHib/void/blob/main/data/area/misthalin/barbarian_village/stronghold_of_security/flesh_crawler/flesh_crawler.sounds.toml)

```toml
[flesh_crawler_attack]
id = 571

[flesh_crawler_death]
id = 572

[flesh_crawler_defend]
id = 573
```

### MIDIs

Midi tune definitions

[lumbridge_church.midis.toml](https://github.com/GregHib/void/blob/main/data/area/misthalin/lumbridge/church/lumbridge_church.midis.toml)

```toml
[church_organ]
id = 147
```

### Jingles

Jingle definitions

[boat.jingles.toml](https://github.com/GregHib/void/blob/main/data/entity/obj/boat/boat.jingles.toml)

```toml
[swamp_boaty]
id = 137

[ogre_boat_travel]
id = 138

[sailing_theme_short]
id = 171

[sailing_theme]
id = 172
```

## Animations

Animation definitions

[pickaxe.anims.toml](https://github.com/GregHib/void/blob/main/data/skill/mining/pickaxe.anims.toml)

```toml
[bronze_pickaxe_chop]
id = 1511
walk = false
run = false

[rune_pickaxe_stuck]
id = 4760

[bronze_pickaxe_stuck]
id = 4761
```

## Graphics (GFX)

Graphical effect definitions for both gfx and projectiles

[aberrant_spectre.gfx.toml](https://github.com/GregHib/void/blob/main/data/area/morytania/slayer_tower/aberrant_spectre/aberrant_spectre.gfx.toml)

```toml
[aberrant_spectre_goo_cast]
id = 334
height = 190

[aberrant_spectre_goo]
id = 335
delay = 28
curve = 5
flight_time = [ 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 ]

[aberrant_spectre_goo_impact]
id = 336
height = 100
```

## Variables

### Players
Player variables

[gameframe.varps.toml](https://github.com/GregHib/void/blob/main/data/entity/player/modal/toplevel/gameframe.varps.toml)

```toml
[xp_counter]
id = 1801
persist = true
format = "int"

[movement]
id = 173
persist = true
format = "map"
default = "walk"
values = { walk = 0, run = 1, rest = 3, music = 4 }
```

### Player bits

Player variable bits

[makeover.varbits.toml](https://github.com/GregHib/void/blob/main/data/entity/player/modal/makeover/makeover.varbits.toml)

```toml
[makeover_body_part]
id = 6091
format = "list"
values = [ "top", "arms", "wrists", "legs" ]

[makeover_facial_hair]
id = 6084
format = "boolean"

[makeover_colour_skin]
id = 6099
format = "int"
```

### Client
Client variable definitions

[world_map.varcs.toml](https://github.com/GregHib/void/blob/main/data/entity/player/modal/world_map/world_map.varcs.toml)

```toml
[world_map_centre]
id = 622
format = "int"

[world_map_marker_player]
id = 674
format = "int"

[world_map_marker_1]
id = 623
format = "int"

[world_map_marker_type_1]
id = 624
format = "map"
default = "yellow"
values = {
  grave = 0,
  yellow = 1,
  blue = 972,
}
```

### Client-strings

[trade.strings.toml](https://github.com/GregHib/void/blob/main/data/social/trade/trade.strings.toml)

```toml
[item_info_examine]
id = 25

[item_info_requirement]
id = 26

[item_info_requirement_title]
id = 34
```

### Custom

Custom server-side variables

[achievement_task.vars.toml](https://github.com/GregHib/void/blob/main/data/achievement/achievement_task.vars.toml)

```toml
[ardougne_elite_rewards]
persist = true
format = "list"
values = [ "catching_some_rays", "abyssal_valet", "you_could_just_knock", "honestly_its_not_a_purse", "almost_made_in_ardougne" ]

[giant_rat_aggressive]
persist = true
format = "boolean"

[giant_rat_defensive]
persist = true
format = "boolean"
```

## Npcs

### Definitions

NPC Definitions

[cow.npcs.toml](https://github.com/GregHib/void/blob/main/data/entity/npc/animal/cow/cow.npcs.toml)

```toml
[cow_default]
id = 81
hitpoints = 80
attack_bonus = -15
combat_def = "cow"
wander_range = 12
immune_poison = true
slayer_xp = 8.0
respawn_delay = 45
height = 30
drop_table = "cow"
categories = ["cows"]
examine = "Converts grass to beef."

[cow_brown]
clone = "cow_default"
id = 397
```

### Spawns

[entrana.npc-spawns.toml](https://github.com/GregHib/void/blob/main/data/area/asgarnia/entrana/entrana.npc-spawns.toml)
```toml
spawns = [
  { id = "chicken_brown", x = 2850, y = 3371 },
  { id = "chicken_brown", x = 2853, y = 3368 },
  { id = "chicken_brown", x = 2853, y = 3373 },
]
```

### Drops

[Drop tables](drop-tables).

[chicken.drops.toml](https://github.com/GregHib/void/blob/main/data/entity/npc/animal/chicken/chicken.drops.toml)

```toml
[chicken_drop_table]
type = "all"
drops = [
    { table = "chicken_primary" },
    { table = "chicken_secondary" },
    { table = "easy_clue_scroll", roll = 300 }
]

[chicken_primary]
type = "all"
drops = [
    { id = "raw_chicken" },
    { id = "bones" }
]

[chicken_secondary]
roll = 128
drops = [
    { id = "feather", amount = 5, chance = 64 },
    { id = "feather", amount = 15, chance = 32 }
]
```

### Patrols

[port_sarim.patrols.toml](https://github.com/GregHib/void/blob/main/data/area/asgarnia/port_sarim/port_sarim.patrols.toml)

```toml
[port_sarim_guard]
points = [
  { x = 3020, y = 3179, level = 2 },
  { x = 3020, y = 3180, level = 2 },
  { x = 3018, y = 3180, level = 2 },
  { x = 3018, y = 3190, level = 2 },
  { x = 3017, y = 3190, level = 2 },
  { x = 3017, y = 3180, level = 2 },
  { x = 3010, y = 3180, level = 2 },
  { x = 3010, y = 3179, level = 2 },
  { x = 3011, y = 3180, level = 2 },
  { x = 3014, y = 3180, level = 2 },
  { x = 3014, y = 3179, level = 2 },
]
```

## Objects
### Definitions

[tree.objs.toml](https://github.com/GregHib/void/blob/main/data/entity/obj/trees.objs.toml)

```toml
[tree]
id = 1276
examine = "One of the most common trees in Gielinor."

[tree_stump]
id = 1342
examine = "This tree has been cut down."

[tree_3]
clone = "tree"
id = 1277
examine = "A healthy young tree."
```

### Spawns

[shortcut.obj-spawns.toml](https://github.com/GregHib/void/blob/main/data/skill/agility/shortcut/shortcut.obj-spawns.toml)

```toml
spawns = [
    { id = "strong_tree", x = 3260, y = 3179, type = 10, rotation = 2 },
]
```

### Teleports

[runecrafting.teles.toml](https://github.com/GregHib/void/blob/main/data/skill/runecrafting/runecrafting.teles.toml)

```toml
[chaos_altar_portal]
option = "Enter"
tile = { x = 2273, y = 4856, level = 3 }
to = { x = 3060, y = 3585 }

[chaos_altar_ladder_down]
option = "Climb-down"
tile = { x = 2255, y = 4829, level = 3 }
delta = { level = -1 }

[chaos_altar_ladder_up]
option = "Climb-up"
tile = { x = 2255, y = 4829, level = 2 }
delta = { level = 1 }
```


## Items
### Definitions

[ore.items.toml](https://github.com/GregHib/void/blob/main/data/skill/mining/ore.items.toml)

```toml
[clay]
id = 434
price = 34
limit = 25000
weight = 1.0
full = "soft_clay"
kept = "Wilderness"
examine = "Some hard dry clay."

[clay_noted]
id = 435

[copper_ore]
id = 436
price = 18
limit = 25000
weight = 2.267
kept = "Wilderness"
examine = "This needs refining."
```

### Spawns

[draynor.item-spawns.toml](https://github.com/GregHib/void/blob/main/data/area/misthalin/draynor/draynor.item-spawns.toml)

```toml
spawns = [
  { id = "poison", x = 3100, y = 3364, delay = 100 },
  { id = "shears", x = 3126, y = 3358, delay = 6 },
  { id = "spade", x = 3121, y = 3361, delay = 100 },
  { id = "bronze_med_helm", x = 3120, y = 3361, delay = 230 },
]
```
### Item-on-item Recipes

[pie.recipes.toml](https://github.com/GregHib/void/blob/main/data/skill/cooking/pie.recipes.toml)

```toml
[part_mud_pie_compost]
skill = "cooking"
level = 29
remove = ["pie_shell", "compost"]
add = ["part_mud_pie_compost", "bucket"]
ticks = 2
message = "You fill the pie with compost."

[part_mud_pie_bucket]
skill = "cooking"
level = 29
remove = ["part_mud_pie_compost", "bucket_of_water"]
add = ["part_mud_pie_water", "bucket"]
ticks = 2
message = "You fill the pie with water."
```

## Interfaces

### Definitions

[bank.ifaces.toml](https://github.com/GregHib/void/blob/main/data/entity/player/bank/bank.ifaces.toml)

```toml
[bank_pin]
id = 13

[bank_deposit_box]
id = 11
type = "main_screen"

[.close]
id = 15

[.inventory]
id = 17
inventory = "inventory"
width = 6
height = 5
options = { Deposit-1 = 0, Deposit-5 = 1, Deposit-10 = 2, Deposit-X = 4, Deposit-All = 5, Examine = 9 }

[.carried]
id = 18

[.worn]
id = 20
```

## Inventories

[bank.invs.toml](https://github.com/GregHib/void/blob/main/data/entity/player/bank/bank.invs.toml)

```toml
[bank]
id = 95
stack = "Always"

[collection_box_0]
id = 523
stack = "Always"
```

## Shops

[karamja.shops.toml](https://github.com/GregHib/void/blob/main/data/area/karamja/karamja.shops.toml)

```toml
[karamja_wines_spirits_and_beers]
id = 29
defaults = [
    { id = "beer", amount = 10 },
    { id = "karamjan_rum", amount = 10 },
    { id = "jug_of_wine", amount = 10 },
]

[davons_amulet_store]
id = 46
defaults = [
    { id = "holy_symbol", amount = 0 },
    { id = "amulet_of_magic", amount = 0 },
    { id = "amulet_of_defence", amount = 0 },
    { id = "amulet_of_strength", amount = 0 },
    { id = "amulet_of_power", amount = 0 },
]
```

## Enums

[Enums](enums)

[picking.enums.toml](https://github.com/GregHib/void/blob/main/data/entity/obj/picking.enums.toml)

```toml
[pickable_message]
keyType = "object"
valueType = "string"
defaultString = ""
values = {
    cabbage_draynor_manor = "You pick a cabbage.",
    potato = "You pick a potato.",
    wheat = "You pick some wheat.",
    cabbage = "You pick a cabbage.",
    flax = "You pick some flax.",
    onion = "You pick an onion.",
}

[pickable_respawn_delay]
keyType = "object"
valueType = "int"
defaultInt = -1
values = {
    cabbage_draynor_manor = 45,
    potato = 30,
    wheat = 30,
    cabbage = 45,
    flax = 5,
    onion = 30,
}
```


And many more... see the full list in [game.properties](https://github.com/GregHib/void/blob/9c82412c374ced55dbcce337e34de8815e7bb9d2/game/src/main/resources/game.properties#L308).