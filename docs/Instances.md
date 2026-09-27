An Instance is an empty area of the game world which is temporarily allocated for a quest custscene, group activity, or minigame so that players can play seperated from other players doing the same activity at the same time.

[Instances](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/map/instance/Instances.kt) can reserve small or large areas depending on the activity.
* A small instance is 2x2 Regions (128x128 tiles) designed for a 1x1 region with padding.
* A large instance is 5x5 Regions (320x320 tiles) designed for a 3x3 region with padding.

> [!NOTE]
> You can read more about instancing and padding at [osrs-docs/instancing](https://osrs-docs.com/docs/mechanics/instancing/)

## Instances

Allocating an instance is straight-forward:

```kotlin
val region = Instances.small() // Allocate a small or large instance

// play the activity until finished, the instance is freed automatically once empty
```

Every 50 ticks (30 seconds) instances with no players inside them are freed automatically, along with any npcs, objects, floor items and dynamic zones left inside. A newly allocated instance always gets at least 30 seconds for players to arrive.

> [!NOTE]
> There are maximum of 1377 small instances and 700 large instances at any given time.

### Timeout

Instances can be kept for a number of minutes after the last player has left, for example so a player can log back into a dungeon:

```kotlin
val region = Instances.large(timeout = 30) // Kept for 30 minutes after it becomes empty
```

### Lookups

A region can be freed and handed to someone else, so each allocation also has a unique key:

```kotlin
val key = Instances.key(region)       // Store alongside the region (e.g. on the player)
Instances.valid(region, key)          // Still the same allocation?
Instances.owner(player.tile)          // Which allocated instance a tile is in (including padding)
Instances.occupied(region)            // Did it have players in it on the last clean-up pass?
```

For players, `smallInstance()`, `largeInstance()` and `joinInstance()` store both, `instance()` returns null once the instance has been freed, and `clearInstance()` unlinks the player from it.


## Dynamic zones

In most servers Instancing is synonymous with being able to dynamically change maps, in Void these are two separate concepts. Instancing is allocating empty map space, Dynamic Zoning is changing what maps is at a given location and sending those changes to players clients.

Dynamic zones can mix and match Zones or whole Regions from multiple maps into one, optionally rotating them.

```kotlin
val zones: DynamicZones by inject()

enterArea("demon_slayer_stone_circle") {
    val instance = Instances.small() // Allocate a region

    val region = Region(12852)
    zones.copy(region, instance) // Copy the map to the region
    val offset = instance.offset(region) // Calculate the new relative location


    player.tele(Tile(3222, 3367).add(offset) // Teleport the player to the correct relative location
    
    // play the cutscene ...
    
    // The dynamic zones are removed and the region freed automatically once empty
}
```

> Dynamic regions aren't limited to instances either, you can modify the game map and it will update for players within sight in real time, although I'm not sure why you'd want to; there are better ways of modifying the map permenantly.


## Cutscenes

Cutscenes frequently use this combination of [Instance](#instance) and [Dynamic Zones](#dynamic-zones) along with hiding the [GameFrame](interfaces#gameframe-interfaces) tabs and fading the game screen out, so there's a helper function for it.

```kotlin
suspend fun CharacterContext.cutscene() {
    val cutscene = startCutscene("the_cutscene_name", region)

    // ... setup the cutscene here 

    // Optional: set a listener for an early exit e.g. the player logs out halfway through
    cutscene.onEnd {
        // Action on early exit e.g. logout
    }

    // play the cutscene

    cutscene.end(this) // Restore the tabs and unlink the player from the instance
}



