Players are the characters controlled by people, they are used to explore the world, interact with other entities, and hold information about what they have collected and accomplished so far.


## Finding players

Most code will already have access to the player in question, in the situations where one player needs to find or reference another player they can be searched for in the worlds list of [`Players`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/player/Players.kt#L9).

You can search for a player by name, index, `Tile` or `Zone`:

```kotlin
val player = Players.indexed(42) // Get player at an index

val player = Players.find("Cow31337Killer") // Get a specific player by name

val playersUnder = Players.at(Tile(3200, 3200)) // Get all players under a tile

val playersNearby = Players.at(Zone(402, 403)) // Get all players in 8x8 area
```

## Adding players

You can subscribe to new players being spawned with:
```kotlin
playerSpawn {
    message("Welcome to Void.")
}
```

## Removing players

Again for most situations removing players is already handled, `player.logout()` is enough for other scenarios. 

You can subscribe to removed players with:

```kotlin
playerDespawn {
    cancelQuickPrayers()
}
```

## Player options

[Scripts](scripts) can subscribe to player options with:

```kotlin
playerOperate("Req Assist") { }
```

> [!TIP]
> See [interactions](interaction) for more info including the difference between operate and approach interactions.


## Player save data

| Data | Description |
|---|---|
| Account name | The username used to log into the game. |
| Password hash | Encrypted value used to verify a correct password. |
| Index | The number between 1-2048 a particular player is in the current game world. |
| Tile | The current position of the player represented as an x, y, level coordinate. |
| [Inventory](inventories) | Various stores of Items from equipment to shops. |
| [Variables](character-variables) | Progress and tracking data for all types of content. |
| Experience | Progress gained in skilling towards a Level. |
| Levels | Boundaries of skilling experience which unlock new content. |
| Friends | List of befriended players that can be messaged directly. |
| Ignores | List of players who's messages will be hidden. |
| Client | The network connection between the persons Game Client and the game server. |
| Viewport | A store of the entities in the visible surrounding area. |
| Body | The players appearance with/without equipment. |
| Mode | The players current style of movement. |
| Visuals | The players general appearance. |
| Instructions | Instructions send by the Client or Bot controller. |
| Options | Possible interactions other entities can have with this player. |
| [Interfaces](interfaces) | All the [Interfaces](interfaces) the player's Client currently has opened. |
| Collision | Strategy for how the player can move about the game world. |
| [ActionQueue](queues) | List of [Queues](queues) currently in progress for the player. |
| [Soft Timer](timers) | [Timers](timers) that are always counting down for the player. |
| [Timers](timers) | Pause-able [Timers](timers) that are counting down for the player. |
| Steps | List of tiles the player plans on walking to in the future. |

