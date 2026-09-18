[Clocks](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/client/variable/Clocks.kt) are used for tracking a future point in time either in game ticks or in seconds.
Unlike [Timers](timers) a clock does not count down and is only removed when stopped or a clocks time is checked and finished.
This means clocks measured in seconds that are saved to a player's file can continue tracking even when the game is offline. Clocks measured in game ticks however should not be saved.

## Game ticks

Here is a basic example where we start a clock for 1 tick to prevent spam clicking an inventory item.
By default clocks are measured in game ticks.

```kotlin
inventoryOption("Bury", "inventory") {
    // Check if we have already buried a bone this game tick
    if (player.hasClock("bone_delay")) {
        // If the player has buried a bone this tick return without doing anything
        return@inventoryOption
    }
    player.start("bone_delay", 1)
    // ... code here will only run once per tick
}
```

## Epoch seconds

Clocks can also be used to measure seconds when the `epochSeconds()` is passed as a base value.

> [!NOTE]
> Info: Epoch also known as the Unix Epoch or Unix time, refers to number of seconds since the 1st of Janurary 1970.

In this example we prevent a potentially malicious player from closing doors for 60 seconds if they are caught closing doors too frequently.

```kotlin
objectOperate("Close", "door_*") {
    // Check if the clock has any time remaining
    val secondsRemaining = player.remaining("stuck_door", epochSeconds())
    if (secondsRemaining  > 0) {
        // If they do pretend doors are stuck and can't be closed
        player.message("The door seems to be stuck.")
        return@objectOperate
    }

    // If a player closes doors too many times in a short duration they might be trying to trap another player.
    if (player.keepsSlammingDoors()) {
        // Start a clock to stop them closing anymore doors for 60 seconds.
        player.start("stuck_door", 60, epochSeconds())
    }
    // Close the door like normal
}
```

By adding the `stuck_door` clock as a [persistant variable](character-variables) to the `*.vars.toml` file we make sure that the player can't reset the clock by logging out and back in.
```toml
[stuck_door]
format = "int"
persist = true
```

> [!TIP]
> See [Character Variables](character-variables) for more details on how variables are saved