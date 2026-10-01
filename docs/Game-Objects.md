Game objects make up the entire game world from scenery and rocks on the floor, to castle walls and trees there are over 3.5 million game objects across the world. The majority of objects are inactive and never change, other objects are active and can be interacted with by [Players](players).

## Configuration

Objects have one type of [config file](config-files#objects) for storing information ending in `.objs.toml`

```toml
[crashed_star_falling_object]
id = 38659
examine = "It's a crashed star."

[crashed_star_tier_9]
id = 38660
collect_for_next_layer = 15
examine = "This is a size-9 star."
```

This information can be obtained using `obj.def["examine", ""]` see [definition extras](definitions#definition-extras) for more info.

### Spawns
Permanent object changes should be added to [`/data/*.obj-spawns.toml`](https://github.com/GregHib/void/blob/main/data/area/misthalin/edgeville/edgeville.obj-spawns.toml) files to be spawned every time the world loads:

```toml
spawns = [
  { id = "crate", x = 3081, y = 3488, type = 10, rotation = 1 },
]
```

## Finding Objects

Objects are all stored in the [`GameObjects`](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/obj/GameObjects.kt) map which can be searched by `Tile` and id, layer or shape:

```kotlin
val tile = Tile(3200, 3200)
val objectsUnder = GameObjects.at(tile) // Get all objects on a tile

val gameObject = GameObjects.find(tile, "large_door_opened") // Get specific object by id

val gameObject = GameObjects.getShape(tile, ObjectShape.WALL_CORNER) // Get specific object by shape

val gameObject = GameObjects.getLayer(tile, ObjectLayer.WALL_DECORATION) // Get specific object by layer
```

## Adding objects

New objects can be spawned with `GameObjects` `add()` function allowing scripts to modify the world at any time

```kotlin
// Temporarily add a fire for 60 ticks
val obj = GameObjects.add("fire", tile, shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 0, ticks = 60)
```
> [!WARNING]
> Adding a object will override existing objects with the same `ObjectLayer`.

## Removing objects

Objects can also be removed temporarily or permanently

```kotlin
objectOperate("Pick", "wheat") {
    target.remove(30) // Remove the object for 30 ticks
    message("You pick some wheat.")
}
```

## Replacing objects

One object can also be replaced with another temporarily or permanently

```kotlin
objectOperate("Slash", "web*") {
    anim("dagger_slash")
    target.replace("web_slashed", ticks = TimeUnit.MINUTES.toTicks(1)) // Replace the object for 100 ticks
}
```

## Object Data

Due to the shear number of objects in the world they are stored with little information compared to other entities, a game object stores:

| Data | Description |
|---|---|
| Id | The type of object as a [String identifier](string-identifiers). |
| Tile | The position of the object as an x, y, level coordinate. |
| [Definition](definitions) | Definition data about this particular type of object. |
| [Shape](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/entity/obj/ObjectShape.kt) | The type of object, used for layering and interaction path finding. |
| Rotation | A number between 0-3 which represents the direction the object is facing. |
