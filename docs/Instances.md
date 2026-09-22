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

// play the activity until finished

Instances.free(region) // Free up the instance for use by something else
```

> [!IMPORTANT]
> Instance regions must be freed up after use otherwise the world could run out of instance spaces.
> There are maximum of 1377 small instances and 700 large instances at any given time.


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
    
    zones.clear(instance) // Remove the dynamic zones
    Instances.free(instance) // Free up the region
}
```

> Dynamic regions aren't limited to instances either, you can modify the game map and it will update for players within sight in real time, although I'm not sure why you'd want to; there are better ways of modifying the map permenantly.


## Cutscenes

Cutscenes frequently use this combination of [Instance](#instance) and [Dynamic Zones](#dynamic-zones) along with hiding the [GameFrame](interfaces#gameframe-interfaces) tabs and fading the game screen out, so there's a helper function for it.

```kotlin
suspend fun CharacterContext.cutscene() {
    val cutscene = startCutscene("the_cutscene_name", region)

    // ... setup the cutscene here 

    // Optional: set a listener which frees up the region if the player logs out halfway through
    cutscene.onEnd {
        // Action on early exit e.g. logout
    }

    // play the cutscene

    cutscene.end(this) // Make sure the instance is freed
}



