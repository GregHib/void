Event handlers are blocks of code which fire when [events](events) happening in the game world are emitted by the game engine, every handler is registered inside of a [script](scripts).

This page focuses on using handlers, for the full API see the parents of [Script.kt](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/Script.kt).

## Basic Handler

A basic `playerSpawn` which runs when a player logs in:

```kotlin
class HelloWorld : Script {
    init {
        playerSpawn {
            message("Welcome to Void.")
        }
    }
}
```

For longer handlers, you can extract the logic:

```kotlin
class HelloWorld : Script {
    init {
        playerSpawn(::spawn)
    }

    fun spawn(player: Player) {
        player.message("Welcome to Void.")
    }
}
```

All examples you can assume are inside a scripts `init {}` block.

## Handler parameters

Some handlers take arguments, usually IDs:

```kotlin
npcSpawn("sheep") {
    say("Baa!")
}
```

IDs can use wildcards:

```kotlin
npcSpawn("cow_*") {
    softTimers.start("eat_grass")
}
```

See [Wildcards](wildcards) for details.

## Handler Scope

Handlers run in the context of the entity that triggered them:
```kotlin
npcSpawn("sheep") {
    say("Baa!") // npc.say("Baa!")
}

playerSpawn {
    say("Baa!") // player.say("Baa!")
}
```

Interface and inventory handlers also use player scope:

```kotlin
interfaceOption("Yes", "quest_intro:startyes_layer") {
    // this == player
}
```

## Interactions


[Interactions](interaction) fire when a Character performs an action on an Entity. They fall into two categories *operate* (adjacent) and *approach* (from a distance).

Player -> Target
```kotlin
npcOperate("Talk-to", "doric") { }

objectOperate("Steal-cowbell", "dairy_cow") { }
```

Item/Interface -> Target
```kotlin
itemOnNPCOperate("wool", "fred_the_farmer_lumbridge") { }
```

NPC -> Target
```kotlin
npcOperateFloorItem("Take") {
}
```

## Interfaces

Interface IDs follow: `interface:component`.

```kotlin
interfaceOpened("bank") { }

interfaceClosed("price_checker") { }

interfaceOption("Show rewards", "quest_intro:hidden_button_txt") { }
```

## Inventories

```kotlin
itemAdded("logs", "inventory") { }

itemRemoved("cowhide", "inventory") { }
```

## Timers

[Timers](timers) have a start, stop and tick event.

```kotlin
timerStart("poison") {
   30 // Game Ticks until next call
}

timerTick("poison") {
    Timer.CONTINUE
}

timerStop("poison") { }
```

The best way to learn is by [exploring existing scripts](https://github.com/GregHib/void/tree/main/game/src/main/kotlin/world/gregs/voidps/world) to see how handlers are used in practise.