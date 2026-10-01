# Void Content Scripting Cheat-Sheet

Quick reference for writing game content.

## Basic [Script](scripts)

```kotlin
class Firelighters : Script {
    init {
        // register event handlers here
        itemOnItem("logs", "*firelighter") { fromItem, toItem ->
            inventory.transaction {
                remove(fromItem.id)
                replace(toItem.id, "${fromItem.id.removeSuffix("_firelighter")}_logs")
            }
        }
    }
}
```

- Scripts are discovered automatically
- Handlers must be registered inside `init {}`
- Never nest one handler inside another (this will throw an error on startup).
- IDs are **string IDs** (e.g. `"banker"`, `"bank_chest_*"`)
- Most ID parameters accept wildcards: `"*"` matches all, `"bank_booth_*"` prefix-matches, `"a,b,c"` matches a list etc...
- Handlers perspectives are of the acting entity - usually `Player` (so `this` is the player), sometimes `NPC`.
- Handlers typed `suspend` (interactions, dialogue, item options) can pause for dialogue, delays, and player input

---

## Spawn / Despawn - lifecycle

```kotlin
playerSpawn { /* this: Player, on login/world-enter */ }
npcSpawn("goblin*") { /* this: NPC */ }
objectSpawn("*") { /* this: GameObject */ }
floorItemSpawn("coins") { /* this: FloorItem */ }
worldSpawn { configFiles -> /* once, on server startup */ }

playerDespawn { }                       // this: Player
playerLogout { /* return Boolean */ }   // false to block logout
npcDespawn("goblin*") { }
objectDespawn("*") { }
floorItemDespawn("coins") { }
worldDespawn { }                        // server shutdown
```

## [Interactions](interaction) - operate vs. approach

`operate` fires when adjacent to the target.
`approach` fires from a distance (detail 10 - set with `approachRange(n)`).

All interaction handlers are `suspend`.
Handler arguments are an interaction context you can destructure, e.g. `{ (target) -> ... }`.

```kotlin
// Player operates a target (adjacent)
npcOperate("Talk-to", "banker*") { /* this: Player; PlayerOnNPCInteract */ }
objectOperate("Use", "bank_chest_*") { open("bank") }
objectOperate("Use", "bank_booth_*", arrive = false) { (target) -> /* skip walking to it */ }
playerOperate("Trade") { }
floorItemOperate("Take") { }

// Player approaches a target (ranged)
npcApproach("Bank", "banker*") { approachRange(2); open("bank") }
objectApproach("Open", "door_*") { }
playerApproach("Attack") { }
floorItemApproach("Telekinetic Grab") { }

// Use an interface item / spell ON a target
itemOnNPCOperate("dragon_bones", "*") { }
itemOnObjectOperate("bucket", "well") { }
itemOnPlayerOperate("ring_of_*") { }
onObjectOperate("staff_of_*", "altar") { }   // interface-component on object
// ...and the matching *Approach variants for each.

// NPC is the actor
npcOperatePlayer("Attack") { /* this: NPC */ }
npcOperateObject("Open", "gate_*") { }
npcApproachNPC("Talk") { }
```

## [Dialogues](dialogues)

```kotlin
npcApproach("Talk-to", "banker*") {
    approachRange(2)
    npc<Quiz>("Good day. How may I help you?")             // NPC line, expression = Quiz
    player<Happy>("Hello!")                                // player line
    choice {
        option<Happy>("Access my bank.") { open("bank") }  // player repeats selected option with expression = Happy
        option("What is this place?") {
            npc<Neutral>("A branch of the Bank of ${Settings["server.name"]}.")
        }
    }
    statement("Some narration.")                         // plain message box
}
```

Low-level continue hooks (rarely needed directly):

```kotlin
continueDialogue("interface_component") { id -> }
continueItemDialogue { item -> }
```

## [Inventory](inventories) and item behaviour (`Items`)

```kotlin
// Inventory change notifications (synchronous)
itemAdded("coins", "inventory") { (item) -> }
itemRemoved("rune_*", "worn_equipment") { }
slotChanged("inventory", slot = 0) { change -> }
slotChanged("worn_equipment", EquipSlot.Weapon) { change -> }
inventoryUpdated("bank") { inventory, changes -> }

// Item lifecycle / rules
bought("*") { item -> }
sold("*") { item -> }
dropped("bones") { item -> }
droppable { item -> /* return Boolean, prevent item dropping */ }
destructible("rune_*") { item -> /* Boolean */ }
destroyed("rune_*") { item -> }
consumable("*food*") { item -> /* Boolean */ }
consumed("*") { item, slot -> /* apply food/potion effects */ }
taken("coins") { floorItem -> }
takeable("rare_*") { floorItem, telegrab -> /* return id to replace or null to cancel */ }
crafted(Skill.Crafting) { def -> /* after item-on-item craft */ }
```

Modify inventories transactionally, either all actions succeed or nothing changes:

```kotlin
inventory.transaction {
    remove("logs")
    add("ashes")
    replace("raw_shrimp", "shrimp")
}

if (inventory.transaction.error == TransactionError.None) {
    // Success
} else {
    // Nothing changes
}
```

## [Interfaces](interfaces)

```kotlin
interfaceOpened("bank") { id -> }
interfaceClosed("bank") { id -> }
interfaceRefresh("skill_guide") { id -> }
interfaceOption("Select", "bank_component") { option -> }   // suspend
itemOption("Wield", "rune_*", inventory = "inventory") { option -> }  // suspend
onItem("interface_component", "item") { item, id -> }
itemOnItem("logs", "*firelighter") { from, to -> }          // combine two items
interfaceSwap("inventory", "inventory") { from, to, fromSlot, toSlot -> }
questJournalOpen("cooks_assistant") { /* this: Player */ }
shopOpen("general_store") { id -> }
```

## [Variables](character-variables)

Player/NPC state is stored as named variables; react to changes:

```kotlin
variableSet("quest_cooks_assistant") { key, from, to -> }
npcVariableSet("phase", "boss_*") { key, from, to -> }   // this: NPC
variableBitAdded("task_reward_items") { value -> }
variableBitRemoved("task_reward_items") { value -> }
```

Read/write from a receiver: `get("key", default)`, `set("key", value)`, `addVarbit("set", "value")`.

## [Timers](timers)

Return value controls the timer: a tick interval, `Timer.CONTINUE`, or `Timer.CANCEL`.

```kotlin
// Player timers
timerStart("poison") { restart -> 5 }          // ticks until first fire (or Timer.CANCEL)
timerTick("poison") { /* return Timer.CONTINUE / CANCEL / new interval */ }
timerStop("poison") { logout -> }

// NPC and world equivalents
npcTimerStart("respawn") { restart -> 100 }
npcTimerTick("respawn") { Timer.CANCEL }
npcTimerStop("respawn") { death -> }
worldTimerStart("shooting_star") { 200 }
worldTimerTick("shooting_star") { Timer.CONTINUE }
worldTimerStop("shooting_star") { shutdown -> }
```

Start one from a receiver with `softTimer("name")` / `strongTimer("name")` (see timer helpers).

## Movement & areas

```kotlin
moved { from -> /* this: Player, from = previous Tile */ }
npcMoved("goblin*") { from -> }
entered("wilderness") { area -> }
exited("wilderness") { area -> }
```

## Skills

```kotlin
levelChanged(Skill.Attack) { skill, from, to -> }   // null skill = any
maxLevelChanged() { skill, from, to -> }
npcLevelChanged(Skill.Hitpoints, "goblin*") { skill, from, to -> }
experience { skill, from, to -> }
blockedExperience { skill, exp -> }
```

Award xp from a receiver: `exp(Skill.Firemaking, 40.0)`.

## Death

```kotlin
playerDeath { onDeath ->                 // mutate the OnDeath data class
    onDeath.dropItems = false
    onDeath.teleport = Tile(3222, 3218)
}
npcDeath("goblin*") { onDeath -> }
npcAfterDeath("boss_*") { /* this: NPC, runs once after death sequence */ }
npcCanDie("immortal_*") { /* return Boolean */ }
```

## [Combat](combat-scripts) and [NPC Combat](npc-combat-scripts)

```kotlin
canAttack("guard*") { target -> /* Boolean */ }
combatPrepare(style = "magic") { target -> /* Boolean, check runes etc. */ }
combatSwing(weapon = "dragon_dagger", style = "stab") { target -> }
combatAttack(style = "melee") { attack -> }        // damage dealt
combatDamage(style = "*") { damage -> }            // damage received (defend gfx)
combatStart { target -> }
combatStop { target -> }
specialAttack("dragon_dagger") { target, id -> }
specialAttackPrepare("dragon_dagger") { id -> /* Boolean */ }
specialAttackDamage("dragon_dagger") { target, damage -> }

// NPC-side combat scripting
npcCombatSwing { target -> }                       // only one allowed
npcAttack("boss_*", "fire_breath") { target -> }
npcImpact("boss_*", "fire_breath") { target -> /* Boolean */ }
npcCondition("enraged") { target -> /* Boolean gate on an attack */ }
npcCombatAttack("boss_*") { attack -> }
```

## NPC [hunting](hunt-modes) / Aggression

NPC searches its surroundings; a matching mode handler decides what to do with the target.

```kotlin
huntPlayer("goblin*", mode = "aggressive") { player -> }   // this: NPC
huntNPC(mode = "social") { otherNpc -> }
huntObject(mode = "forage") { obj -> }
huntFloorItem(mode = "scavenge") { floorItem -> }
```

## Teleport

```kotlin
teleportTakeOff("ancient") { spell -> /* Boolean, gate take-off */ }
teleportRemoveItems("ancient") { spell -> /* Boolean, consume runes */ }
teleportLand("ancient") { /* this: Player, on arrival */ }
objTeleportTakeOff("Use", "spirit_tree") { obj, option -> Teleport.CONTINUE }  // or CANCEL
objTeleportLand("Use", "spirit_tree") { obj, option -> }
```

Trigger a teleport with `Teleport.teleport(player, tile / "area", type = "modern", ...)`.

## Misc

```kotlin
settingsReload { /* re-read config after a /reload */ }
```

---

## Common receiver helpers

These come from the `Player` / `NPC` / character extensions, available inside handlers (not from `Script`):

| Call | Effect |
|------|--------|
| `message("text")` | Send a game chatbox message |
| `anim("name")` / `gfx("name")` | Play animation / graphic by string id |
| `sound("name")` | Play a sound |
| `open("interface")` / `close()` | Open / close an interface |
| `inventory` / `bank` / `equipment` | Access named inventories (use `.transaction { }` to mutate) |
| `get("key", default)` / `set("key", v)` | Read / write a variable |
| `addVarbit("set", "value")` / `clearVarbit(...)` | Bitfield variables |
| `exp(Skill.X, amount)` | Award experience |
| `hasClock("name")` / `start("name", ticks)` | Cooldown/delay [clocks](clocks) |
| `delay(ticks)` | Suspend the current action (only in `suspend` handlers) |
| `talkWith(npc) { ... }` | Open a dialogue scope with an NPC |

