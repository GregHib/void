## Adding default shops

Find the shop in various `*.shops.toml` files, or add it if you know the inventory id
```toml
[bobs_brilliant_axes]
id = 1
defaults = [
    { id = "bronze_pickaxe", amount = 10 },
    { id = "bronze_hatchet", amount = 10 },
    { id = "iron_hatchet", amount = 10 },
    { id = "steel_hatchet", amount = 10 },
    { id = "iron_battleaxe", amount = 10 },
    { id = "steel_battleaxe", amount = 10 },
    { id = "mithril_battleaxe", amount = 10 },
]
```

Add the shopkeeper to a `*.npcs.toml` file
```toml
[bob]
id = 519
categories = ["human"]
shop = "bobs_brilliant_axes"
examine = "An expert on axes."
```
> `shop =` needs to be set to the inventory string id for shops to open automatically when clicking the "Trade" option

Spawn the shopkeeper in the right place in a `*.npc-spawns.toml` file

```toml
spawns = [
  { id = "bob", x = 3228, y = 3203, level = 0, direction = "SOUTH" },
]
```

## Opening a shop

Npcs with the `Trade` option will open shops listed in their npcs.toml automatically however when opened via dialogue options the `OpenShop` event should be used:

```kotlin
player.openShop("zekes_superior_scimitars")
```

## General stores

To add a general store which all players can see and most tradable items can be sold to, simply make sure the inventory name ends with `general_store`, for example:

```toml
[lumbridge_general_store]
id = 24
defaults = [
    { id = "empty_pot", amount = 30 },
    { id = "jug", amount = 10 },
    { id = "shears", amount = 10 },
    # etc...
]
```