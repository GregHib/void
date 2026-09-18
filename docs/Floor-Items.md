Floor items are items that are either spawned with `*.item-spawns.toml`, [dropped](drop-tables) by [Monsters](npcs) or by other [Players](players). They can be picked up to move them into a [Players](players) [Inventory](inventories).

An item that is dropped will only be shown to the person it was dropped by/for and after `revealTicks` it will be shown to all [Players](players), `disappearTicks` after that it will be permanently deleted from the world.

## Configuration

### Definitions

[`*.items.toml`](https://github.com/GregHib/void/blob/main/data/skill/farming/basket.items.toml)

```toml
[basket]
id = 5376
price = 1
limit = 1000
weight = 0.028
examine = "An empty fruit basket."

[basket_noted]
id = 5377

[apples_1]
id = 5378
weight = 0.056
empty = "basket"
examine = "A fruit basket filled with apples."
```


### Spawns

Permanent and re-spawning floor items should be added to [`/data/*.item-spawns.toml`](https://github.com/GregHib/void/blob/main/data/minigame/blast_furnace/blast_furnace.item-spawns.toml) [config files](config-files#items):

```toml
spawns = [
  { id = "spade", x = 1951, y = 4964, delay = 100, members = true },
]
```

## Finding floor items
Much the same as other entities, `FloorItems` contains and can be used to control all the floor items in the world, it can be found searched for by `Tile` or `Zone`:

```kotlin
val itemsUnder = FloorItems.at(Tile(3200, 3200)) // Get all items on a tile

val itemsNearby = FloorItems.at(Zone(402, 403)) // Get all items in 8x8 area
```

## Adding floor items

Items can be spawned onto the floor with:

```kotlin
val floorItem = FloorItems.add(tile, "ashes", revealTicks = 90, disappearTicks = 60)
```

## Removing floor items

Floor items can also be removed with:

```kotlin
npcFloorItemOperate("Take") { (target) ->
    FloorItems.remove(target)
}
```

## Floor item data

| Data | Description |
|---|---|
| Id | The type of item as a [String identifier](string-identifiers). |
| Amount | The amount of the item in this [stack](inventories#item-stack-behaviour). |
| [Definition](definitions) | Definition data about this particular type of item. |
| Tile | The position of the item represented as an x, y, level coordinate. |
| Reveal ticks | Number of ticks before an item is made public for everyone. |
| Disappear ticks | Number of ticks before the item is permanently deleted. |
| Owner | The original owner of the item. |