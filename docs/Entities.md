Entity is a broad category for everything that exists in the game world, with a second sub-category `Character` for Player and NPC entities that can move. The main Entity types are:

* Blue - [Players](players)
* Green - [NPCs](npcs) (Non-Player Characters)
* Orange - [Objects](game-objects) (GameObjects)
* Purple - [Floor items](floor-items)

![Screenshot 2024-02-20 171650](https://github.com/GregHib/void/assets/5911414/268ba82d-be17-4b0e-b3ca-e52e37f8e152)

> [!NOTE]
> Item's are considered a part of [interfaces](interfaces) not entities, for more info see [Inventories](inventories).

# World
Although only an entity due to technicality the World also has a number of operations the same as other entities.

World spawn and despawn are called on server startup and shutdown:

```kotlin
worldSpawn {
}

worldDespawn {
}
```

And world timers work the same as other entities:

```kotlin
worldTimerStart("timer_name") {
}
worldTimerTick("timer_name") {
}
worldTimerStop("timer_name") {
}
```