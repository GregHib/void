Queues are an alternative to [delays](https://osrs-docs.com/docs/mechanics/delays/) for executing code after a set amount of time. In most cases a delay should be used when [interacting](interaction) however in some cases, like during interface interactions, you may need to use a queue to delay to a later point in time. Unlike [Timers](timers) a Queue only runs once.

There are 4 types of [Queues](https://github.com/GregHib/void/blob/a4af3f90fdc33c890935abc9531f903e06b1a6d0/engine/src/main/kotlin/world/gregs/voidps/engine/queue/ActionPriority.kt#L3): 
* [Weak](#weak-queues) - Removed by interuptions and String queues
* [Normal](#normal-queues) - Skipped if interface is open
* [Strong](#strong-queues) - Closes interfaces and cancels Weak actions
* [Soft](#soft-queues) - Closes interfaces and paused by suspensions but not by other queues

> [!NOTE]
> Read more details about queues and how they work on [osrs-docs/queues](https://osrs-docs.com/docs/mechanics/queues/).

Queues are easy enough to start and can be started on any [Character](entities), they only require a unique name and an initial delay in ticks:

## Normal queues

Normal queues are for regular, do something in a few ticks or do something suspendable now
```kotlin
player.queue("welcome") {
    statement("Welcome to Lumbridge! To get more help, simply click on the Lumbridge Guide or one of the Tutors - these can be found by looking for the question mark icon on your minimap. If you find you are lost at any time, look for a signpost or use the Lumbridge Home Teleport spell.")
}
```

## Weak queues

Weak queues are often used for item-on-item interactions and other interruptable skills
```kotlin
player.weakQueue("cast_silver", 3) {
    inventory.replace("silver_bar", data.item)
    exp(Skill.Crafting, data.xp)
    make(item, amount - 1)
}
```

## Strong queues

Strong queues are mainly used for non-interruptable events such as [Hits](combat-scripts#calculations), emotes and dying:

```kotlin
player.strongQueue("teleport") {
    player.playSound("teleport")
    player.gfx("teleport_$type")
    player.animDelay("teleport_$type")
    player.tele(tile)
}
```

> [!NOTE]
> Teleports don't have to be implemented manually, see [Teleport](https://github.com/GregHib/void/blob/main/game/src/main/kotlin/content/skill/magic/spell/Teleport.kt).

## Soft queues

Soft queues are used for things that have to happen and nothing short of death will stop them.
```kotlin
player.softQueue("remove_ammo") {
    player.equipment.remove(ammo, required)
}
```

# World queues

World queues work slightly differently than character queues as they are designed for more for scheduling singular events to occur against real time. For example distractions & diversions, or starting mini-games like castle wars. As such any World action queued will override any previous action queued with the same name.

```kotlin
World.queue("shooting_star_event_timer", TimeUnit.MINUTES.toTicks(minutes)) {
    startCrashedStarEvent()
}
```