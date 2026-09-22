# Interactions
| Action | Code | Links |
|--------|------|-------|
| Wait X ticks | `delay(ticks)` | |
| Nearby object | `objectOperate("Action", "object_id") {}` | |
| Distant object | `objectApproach("Action", "object_id") {}` | |
| Item on nearby object | `itemOnObjectOperate("Action", "npc_id") {}` | |
| Item on distant object | `itemOnObjectApproach("Action", "npc_id") {}` | |
| Nearby npc | `npcOperate("Action", "npc_id") {}` | |
| Distant npc| `npcApproach("Action", "npc_id") {}` | |
| Item on nearby npc | `itemOnNPCOperate("Action", "npc_id") {}` | |
| Item on distant npc | `itemOnNPCApproach("Action", "npc_id") {}` | |

# Players
| Action | Code | Links |
|--------|------|-------|
| Message| `message("Welcome to Void.")` | |
| Animation | `anim("sound_id")` | |
| Graphical effect | `gfx("gfx_id")` | |
| Area graphical effect | `areaGfx("gfx_id", tile, radius)` | |
| Projectile | `shoot(target, "gfx_id")` | |
| Projectile from tile | `tile.shoot(target, "gfx_id")` | |
| Play sound | `sound("sound_id")` | |
| Play area sound | `areaSound("sound_id", tile, radius)` | |
| Teleport | `tele(x, y)` | |
| Check current level | `has(Skill.Attack, 10)` | |
| Check actual level | `hasMax(Skill.Constitution, 500)` | |
| Give experience | `exp(Skill.Defence, 20.0)` | |
| Drain skill | `hasMax(Skill.Constitution, 500)` | |
| Within area | `tile in Areas["area_name"]` | |

## Dialogues
| Action | Code | Links |
|--------|------|-------|
| Enter number | `val count = intEntry("Question?")` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/misthalin/zanaris/puro_puro/Elnock.kt#L175) |
| Enter string | `val string = stringEntry("Question?")` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/entity/player/modal/tab/Notes.kt#L28) | 
| Warning | `if (!warning("shantay_pass")) {}` | [Example] (https://github.com/GregHib/void/blob/05f3fef86fe6f105dad762629d5876165f2b241d/game/src/main/kotlin/content/area/asgarnia/dwarven_mines/living_rock_caverns/LivingRockCaverns.kt#L24) |

# NPCs

| Action | Code | Links |
|--------|------|-------|
| Spawn | `val npc = NPCs.add("id", tile", Direction.NORTH)` ||
| Animate | `npc.anim("animation_id")` ||
| Find at tile | `val npc = NPCs.find(tile, "npc_id")` ||
| List at tile | `val npcs = NPCs.at(tile)` ||
| List in region | `val npcs = NPCs.at(regionLevel)` ||
| Remove | `NPCs.remove(npc)` ||

# Items

| Action | Code | Links |
|--------|------|-------|
| Equip at slot | `equipped(EquipSlot.Hands)` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/misthalin/edgeville/Nettles.kt#L25) [EquipSlot](https://github.com/GregHib/void/blob/main/network/src/main/kotlin/world/gregs/voidps/network/login/protocol/visual/update/player/EquipSlot.kt) |
| Check equipped | | |
| Item in inv, equip or bank | `ownsItem("item_id", amount)` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/asgarnia/falador/SquireAsrol.kt#L84) |
| Item equipped or in inventory | `carriesItem("item_id", amount)` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/asgarnia/falador/SquireAsrol.kt#L76) |
| Contains | `inventory.contains("item_id", amount)` | |
| Count | `val count = inventory.count("item_id")` | |
| Full | `inventory.isFull()` | |
| At index | `inventory[index]` | |
| Add item | `inventory.add("item_id", amount)` | |
| Remove item | `inventory.remove("item_id")` | |
| Transaction | `inventory.transaction {}` | |

# Objects
| Action | Code | Links |
|--------|------|-------|
| Spawn | `GameObjects.add("object_id", tile, ticks)`| [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/kharidian_desert/al_kharid/AlTheCamel.kt#L159)|
| Animate | `obj.anim("animation_id")` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/misthalin/lumbridge/castle/LumbridgeFlag.kt#L9) |
| Find at tile | `GameObjects.find(tile, "object_id")` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/asgarnia/dwarven_mines/living_rock_caverns/LivingRockCaverns.kt#L33) |
| Replace | `oldObject.replace("replacement_id")` | [Example](https://github.com/GregHib/void/blob/f1bcd0960df8ae2dd6b328a82bf6bf5bc9b62e07/game/src/main/kotlin/content/area/asgarnia/falador/SirVyvin.kt#L34) |
| List on tile | `GameObjects.at(tile)` | |
| Get by shape | `GameObjects.getShape(tile, shape)` | [ObjectShape](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/obj/ObjectShape.kt) |
| Get by layer | `GameObjects.getLayer(tile, shape)` | [ObjectLayer](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/obj/ObjectLayer.kt) |
| Enter door | `enterDoor(door)` | Force walk through a door without letting other players through it. [Example](https://github.com/GregHib/void/blob/a543a8b41fc8f16394fd98c57fa5ef4c2375e98c/game/src/main/kotlin/content/area/misthalin/zanaris/Gatekeeper.kt#L32) |

TODO: get in area

# Floor items

# World

| Action | Code | Links |
|--------|------|-------|
| Run code later | `World.queue("unique_name", ticks) {}` ||

