Non-Player-Characters exist in the game for [Players](players) to interact with, talk to for quests, information or [shops](shops), and fight in [combat](combat-scripts) although these are typically referred to as Monsters.

While NPCs have a lot of similarities with [Players](players) as both are `Characters`, NPCs have a lot data due to not being connected to a `Client` and thus having no concept of [Interfaces](interfaces) or [Inventories](inventories).

## Configuration

NPCs have two types of [config files](config-files#npcs) which store information which doesn't exist in the cache.

- `.npc.toml` files store additional information such as combat stats, animations and examines.
- `.npc-spawns.toml` files store spawn information

```toml
[cow_default]
id = 81
hitpoints = 80
attack_bonus = -15
wander_radius = 12
immune_poison = true
slayer_xp = 8.0
combat_def = "cow"
respawn_delay = 45
drop_table = "cow"
categories = ["cows"]
height = 30
examine = "Converts grass to beef."
```

This information can be obtained using `npc.def["examine", ""]` see [definition extras](definitions#definition-extras) for more info.


### Spawns

Permanent and respawning npcs are configured in `/data/*.npc-spawn.toml` files organised by location.
```toml
spawns = [
  { id = "cow_default", x = 2936, y = 3274 },
  { id = "cow_brown", x = 2921, y = 3287 },
  { id = "cow_brown", x = 2933, y = 3274 },
  { id = "cow_light_spots", x = 2924, y = 3288 },
]
```

One-off npc spawns (like for quests or bosses) can be added using `NPCs`:

```kotlin
val cow = NPCs.add("cow_brown", Tile(3200, 3200), Direction.NORTH)
```

## Finding NPCs

Much like [Players](players#finding-players) NPCs can be found using the world [`NPCs`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/character/npc/NPCs.kt) and can be searched for by id, index, `Tile` or `Zone`:

```kotlin
val npc = NPCs.indexed(42) // Get npc at an index

val npc = NPCs.find(tile, "chicken") // Get a specific npc by id

val npcsUnder = NPCs.at(Tile(3200, 3200)) // Get all npcs under a tile

val npcsNearby = NPCs.at(Zone(402, 403)) // Get all npcs in 8x8 area
```

## Adding NPCs

[Scripts](scripts) can subscribe to npc spawns with:

```kotlin
npcSpawn("cow*") {
    softTimers.start("eat_grass")
}
```

## Removing NPCs

NPCs can be removed easily with:

```kotlin
npcs.remove(cow)
```

And [Scripts](scripts) can subscribe to despawns using:

```kotlin
npcDespawn {
    softTimers.stopAll()
}
```

## NPC options

NPC [interactions](interaction) can be subscribed to as well:

```kotlin
npcApproach("Talk-to", "banker*") { }

npcOperate("Talk-to", "zeke") { }
```

> [!TIP]
> See [interactions](interactions) for more info including the difference between operate and approach interactions.

## NPC data

| Data | Description |
|---|---|
| Id | The type of npc as a [String identifier](string-identifiers). |
| Index | The number between 1-32768 a particular npc is in the current game world. |
| Tile | The current position of the npc represented as an x, y, level coordinate. |
| Levels | Fixed levels used in [Combat calculations](combat-scripts). |
| Mode | The npcs current style of movement. |
| [Definition](definitions) | Definition data about this particular type of npc. |
| [ActionQueue](queues) | List of [Queues](queues) currently in progress for the npc. |
| [Soft Timer](timers) | The one [Timer](timers) that is currently counting down for the npc. |
| [Variables](character-variables) | Progress and tracking data for different types of content. |
| Steps | List of tiles the player plans on walking to in the future. |
| Collision | Strategy for how the npc can move about the game world. |
| Visuals | The npcs general appearance. |