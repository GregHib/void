A lot of [Entities](entities) are interactable, allowing other entities to perform actions on them like Talk-to, Follow, Attack, Trade etc... [Interact](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/mode/interact/Interact.kt) is a [Movement Mode](movement-mode) which moves a character in range of a target entity before performing an interaction.

```kotlin
val interaction = FloorItemOption(player, floorItem, "Light")
player.mode = Interact(player, floorItem, interaction)
```

## Interaction types
Interactions fall into two categories: Approach and Operate.

* Approach interactions trigger when the character is within 10 tiles of the target, this is mainly used for [ranged combat](combat-scripts) and magic spells.
* Operate interactions are triggered when the character is next to the target.

> [!NOTE]
> In RuneScript these are called `ap` and `op` scripts which stand for Approachable and Operable. We have used alternative verbs.
>
> You can read more about interactions at [osrs-docs/entity-interactions](https://osrs-docs.com/docs/mechanics/entity-interactions/).

There are many types of interactions:

| Interaction | Description |
|---|---|
| [`InterfaceOption`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/client/ui/InterfaceOption.kt) | Clicking [Interfaces](interfaces) or Item . |
| [`Command`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/client/ui/event/Command.kt) | A command typed into the client console. |
| [`ItemOn...`](https://github.com/GregHib/void/tree/main/engine/src/main/kotlin/world/gregs/voidps/engine/client/ui/interact) | Using an item on a type of [Entity](entities). |
| [`NPCOption`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/npc/NPCOption.kt) | An option on an [NPC](entities#npcs). |
| [`PlayerOption`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/player/PlayerOption.kt) | An option on a [Player](entities#players). |
| [`FloorItemOption`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/item/floor/FloorItemOption.kt) | An option on a [Floor item](entities#floor-items). |
| [`ObjectOption`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/obj/ObjectOption.kt) | An option on a [Game object](entities#game-objects). |
| [`CombatInteraction`](https://github.com/GregHib/void/blob/main/game/src/main/kotlin/world/gregs/voidps/world/interact/entity/combat/CombatInteraction.kt) | In [combat](combat-scripts) with a target. |
| [`InventoryOption`](https://github.com/GregHib/void/blob/main/game/src/main/kotlin/world/gregs/voidps/world/interact/entity/player/equip/InventoryOption.kt) | An option on an [Inventory item](inventories) on an Interface. |

## Entity interactions

Most entity interactions have at least 4 helper functions: two for player operating or approaching an entity, and two for a character operating or approaching an entity. Their usage and will vary depending on the interaction and the [entity](entities) being interacted with. See [each class](#interaction-types) for the complete list of functions available.

### Npcs

```kotlin
npcOperate("Talk-to", "ellis") {
}
npcApproach("Talk-to", "banker*") {
}
characterOperateNPC("Follow") {
}
characterApproachNPC("Attack") {
}
```

### Commands

```kotlin
command("reset_cam") {
    player.client?.clearCamera()
}
modCommand("clear") {
    player.inventory.clear()
}
adminCommand("bank") {
    player.open("bank")
}
```

### Inventories

```kotlin
inventoryOptions("Eat", "Drink", "Heal") {
}
inventoryOption("Bury", "inventory") {
}
inventoryItem("Rub", "amulet_of_glory_#", "inventory") {
}
```

### Operation Arrival

Most cases you'll want to wait until the player is right next to the target before interaction. 

- todo pic

However when an interaction pops up an [interfaces](interfaces) to begin with interactions should start sooner by specifying `arrive = false` in the interaction parameters:

```kotlin
itemOnObjectOperate("*_mould", "furnace*", arrive = false) {
    player.open("make_mould${if (World.members) "_slayer" else ""}")
}
```

## Wildcards

When interacting with entities often times you'll want to write code to interact with more than one at a time, for example bankers all have the same [dialogues](dialogues) so instead of writing an npc approach script for `banker_al_kharid`, `banker_zanaris`, `banker_shilo_village` etc.. individually you can write one script with a [wildcard character](https://support.microsoft.com/en-gb/office/examples-of-wildcard-characters-939e153f-bd30-47e4-a763-61897c87b3f4) to match a pattern.

Currently Void supports two wildcard characters:

| Symbol | Pattern |
|---|---|
| `*` | Matches any amount of any character |
| `#` | Matches a single digit |

So you can match all bankers using `banker_*` instead of listing them individually.
Note: The real wildcard uses `banker*` to also match `banker` as well.

Matching a single digit is useful mainly for items with [charges](degrading#charges) like jewellery and potions. `amulet_of_glory_#` will match `amulet_of_glory_1`, `amulet_of_glory_2`, `amulet_of_glory_4` etc... (but not `amulet_of_glory_10` which in most cases is fine as charges rarely go above 8).

The same applies for items, interfaces, components and all other [string ids](string-identifiers) used in [script functions](scripts) unless otherwise specified.
