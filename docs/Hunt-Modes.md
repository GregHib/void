Hunt modes are used by [NPCs](entities#npcs) to select targets from a collection of nearby [Entities](entities). Properties defined in `hunt_modes.toml` determines under what conditions entities can be targeted, which entities can be targeted, how often a new target is selected.

Hunt modes are use for mechanics such as aggression and hunter npcs getting caught in traps.

> [!NOTE]
> Aggression is usually `hunt_mode = "cowardly"` meaning aggressive so long as within a combat level threshold. `"aggressive"` hunt mode is used exclusively for being aggressive to players at all times

## Hunt modes

Hunt modes are specified in [`hunt_modes.toml`](https://github.com/GregHib/void/blob/main/data/entity/npc/hunt_modes.toml):
```toml
[cowardly]                     # Name of the hunt mode
type = "player"                # Target entity type
check_visual = "line_of_sight" # Only select targets that can be seen directly
check_not_combat = true        # Target can't be in combat
check_not_combat_self = true   # NPC can't be in combat
check_not_too_strong = true    # Targets combat level must be less than double of npc 
check_not_busy = true          # Target can't be doing something
```
And a hunt mode is assigned to an npc in it's `.npcs.toml` file:

```toml
[giant_spider]
id = 59
hunt_mode = "cowardly" # Hunt mode name
hunt_range = 1         # Check for entities up to 1 tile away
```

When a valid target is found a hunt event will be emitted:

```kotlin
huntFloorItem("ash_finder") { target ->
    interactFloorItem(target, "Take")
}
```

> Hunt events are: `HuntFloorItem`, `HuntNPC`, `HuntObject`, `HuntPlayer`

> [!NOTE]
> [Read more about hunt modes](https://github.com/GregHib/void/issues/307)