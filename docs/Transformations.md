Not every player sees the same thing. A quest NPC might be a chicken, a man or invisible depending on what stages of a quest a player is in, a door can be locked for one player and open for the next, and a bush might only have berries for those who haven't picked it. These are all done with transformations.

Instead of spawning different npcs or objects for each player, one entity is placed in the world and a [variable](character-variables) on the player decides which version of it they see.

## How it works

Some npc and object [definitions](definitions) in the cache contain a transform list: a variable (a varbit or varp) and a list of other definitions to pick from.

If a player's varbit is `1` they see `transforms[1]`, if it is `2` they see `transforms[2]` and so on. If the value is higher than the list is long, or the entry is `-1`, the last entry in the list is used.

> [!NOTE]
> The client does all the work of picking what to show, the server never sends "this npc looks like X" it only sends the variable.

## Changing what a player sees

All that's needed to change what a player sees is to set the variable, there's no need to touch the npc or object at all.

```kotlin
set("thzfe_sithik_transformed", true)
```

The variable has to be configured in a [`*.varbits.toml`](character-variables#player-variables) (or `*.varps.toml`) file with the same id as the one in the cache, otherwise the server won't know which variable to read and will treat it as `0`.

```toml
[thzfe_sithik_transformed]
id = 495
persist = true
format = "boolean"
```

> [!TIP]
> Using `format = "map"` with `values` lets scripts use readable names like `"ogre"` instead of numbers, the server converts them back to an index when it needs to.

## Using the transformed version

Each version has its own [string identifier](string-identifiers) and config like any other npc or object, so they can have different options, examines and dialogue.

```toml
[zogre_sithik_man]
id = 2061

[zogre_sithik_ogre]
id = 2062
dialogue = "ogre"
```

The server uses the same logic to work out which options a player is allowed to click, so each player is only able to interact with the version they can see.

> [!WARNING]
> Scripts subscribe to a specific id, so `npcOperate("Talk-to", "zogre_sithik_man")` will only trigger for those players who currently see the man, make sure every version you want to handle has a script.

If a script needs to know which version a player is seeing it should check the variable that controls it, as the server only ever has the entity's base definition to hand.

```kotlin
val sithik = if (get("thzfe_sithik_transformed", false)) "zogre_sithik_ogre" else "zogre_sithik_man"
```

## Same for everyone

Transforms in the cache are per player, if every player should see the change (a rock crab waking up, a penguin disguising itself) use `transform()` on an npc instead.

```kotlin
npc.transform("rock_crab_awake")
```

And revert it with:

```kotlin
npc.clearTransform()
```

This changes the npc's [visuals](npcs#npc-data) which are sent to every nearby player, and it's cleared automatically when the npc dies.

> [!NOTE]
> `transform()` also works on players, turning them into an npc. This has nothing to do with the cache transform list and is only for effects like the `::tfm` command.

