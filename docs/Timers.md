Timers count down every X number of ticks; where X is an `interval` specified on the start of the Timer.
Timers are used in situations where *something* must occur at the start, during or end of the countdown, otherwise a [Clock](clocks) is better suited.

There are two types of [Timers](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/timer/Timers.kt):

| Timer type | Applies to | Stops counting down |
|---|---|---|
| Soft | Player, NPC | Never |
| Regular | Player | When busy (delayed or interface open) |

> [!WARNING]
> NPC's are limited to one soft timer at a time, Player's have no such limits.

## Basic Timer

A basic timer is composed of three parts: start, tick & stop.
Start is the only part required as every timer must have an `interval`.

```kotlin
timerStart("skull") { player: Player ->
    // Display a skull over the players head
    player.appearance.skull = player["skull", 0]
    player.flagAppearance()
    1000 // A skull lasts 1000 ticks / 10 minutes
}

timerStop("skull") { player: Player ->
    // When the timer stops remove the skull sprite from above the players head
    player.appearance.skull = -1
    player.flagAppearance()
}

// A player is marked as skulled when they attack a new player in the wilderness
combatStart { target ->
    if (inWilderness && target is Player && !attackers.contains(target)) {
        // Start the skull timer
        skull()
    }
}
```

## Timer
In this example a player starts an `overload` timer which boosts their levels every 15 seconds for 5 minutes in exchange for 500 hitpoints which are returned after the effect wears off.

```kotlin
// Start
timerStart("overload") { player: Player ->
    // Deal 5 hits of 100
    player.queue(name = "overload_hits") {
        repeat(5) {
            player.directHit(100)
            player.anim("overload")
            player.gfx("overload")
            pause(2)
        }
    }
    TimeUnit.SECONDS.toTicks(15) // Every 25 ticks / 15 seconds
}

// Every 25 ticks
timerTick("overload") { player: Player ->
    // Cancel if 5 minutes are up.
    if (player.dec("overload_refreshes_remaining") <= 0) {
        return@timerTick Timer.CANCEL
    }

    // Restore boosted levels every 25 ticks
    player.levels.boost(Skill.Attack, 5, 0.22)
    player.levels.boost(Skill.Strength, 5, 0.22)
    player.levels.boost(Skill.Defence, 5, 0.22)
    player.levels.boost(Skill.Magic, 7)
    player.levels.boost(Skill.Ranged, 4, 0.1923)
Timer.CONTINUE
}

// Stop
timerStop("overload") { player: Player ->
    // Tell the player the overload has worn off
    player.message("<dark_red>The effects of overload have worn off and you feel normal again.")

    // Restore the players levels to normal and return the hitpoints.
    player.levels.restore(Skill.Constitution, 500)
    player.levels.clear(Skill.Attack)
    player.levels.clear(Skill.Strength)
    player.levels.clear(Skill.Defence)
    player.levels.clear(Skill.Magic)
    player.levels.clear(Skill.Ranged)
}
```

It can be triggered using `timers.start()`
```kotlin
// When an overload potion is consumed
consume("overload_#") { player: Player ->
    player.timers.start("overload") // Start the overload timer
    player["overload_refreshes_remaining"] = 20 // Repeat for 5 minutes
}
```
> [!NOTE]
> As the overload effect is a regular timer it's effects can be prolonged by keeping a menu open when not in combat. This also applies to other effects such as poison.

### Restarting
If a timer should persist after a player logs out, it must be restarted once they log back in

```kotlin
playerSpawn {
    if (get("overload_refreshes_remaining", 0) > 0) {
        timers.restart("overload")
    }
}
```

This will call `timerStart` again, but the `restart` allows code that should only be executed on the initial start to do so.
```kotlin
timerStart("overload") { player: Player ->
    if (restart) {
        return@timerStart TimeUnit.SECONDS.toTicks(15) // If restarted don't deal damage again.
    }
    player.queue(name = "overload_hits") {
        // ...
    }
    TimeUnit.SECONDS.toTicks(15)
}

```
### Checking for a timer

You can check if a timer is currently active using the `contains(name)` method.
```kotlin
if (player.timers.contains("overload")) {
    player.message("You may only use this potion every five minutes.")
    return
}
```