package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.tools.map.obj.GameObjectOption
import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext

val ladderOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when(opt) {
        "climb down" -> {
            when(target.opt) {
                "climb up" -> if (obj.tile.plane > target.obj.tile.plane) 1.0 else 0.8
                "climb" -> 0.6
                else -> 0.0
            }
        }
        "climb up" -> {
            when {
                target.opt == "climb down" -> if (obj.tile.plane > target.obj.tile.plane) 1.0 else 0.8
                target.obj.def.name.isTrapDoor() -> 0.7
                target.opt == "climb" -> 0.6
                else -> 0.0
            }
        }
        else -> 0.0
    }
}

val ladderType: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val name = obj.def.name.toLowerCase()
    val targetName = target.obj.def.name.toLowerCase()
    if (name.isLadder() && targetName.isLadder()) {
        1.0
    } else if (name.isStair() || targetName.isStair()) {
        0.0
    } else {
        0.8
    }
}

fun String.isLadder() = contains("ladder") || contains("rope") || contains("chain") || contains("vine") || isTrapDoor()

fun String.isTrapDoor(): Boolean {
    val name = replace(" ", "").toLowerCase()
    return name == "trapdoor" || name == "manhole"
}

val interactTileDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when {
        hasOppositeTile(obj, availableTiles, target.obj, target.tiles) -> 1.0
        hasVerticalTile(obj, availableTiles, target.obj, target.tiles) -> 0.9
        availableTiles.isNotEmpty() && target.tiles.isNotEmpty() -> 0.8
        else -> 0.0
    }
}

private fun hasOppositeTile(obj1: GameObject, tiles1: Set<Tile>, obj2: GameObject, tiles2: Set<Tile>): Boolean {
    for (dir in Direction.cardinal) {
        if (check(obj1, tiles1, dir) && check(obj2, tiles2, dir.inverse())) {
            return true
        }
    }
    return false
}

private fun hasVerticalTile(obj1: GameObject, tiles1: Set<Tile>, obj2: GameObject, tiles2: Set<Tile>): Boolean {
    for (dir in Direction.values) {
        if (check(obj1, tiles1, dir) && check(obj2, tiles2, dir)) {
            return true
        }
    }
    return false
}

private fun check(obj: GameObject, tiles: Set<Tile>, dir: Direction): Boolean {
    val tile = when (dir) {
        Direction.NORTH -> obj.tile.addY(obj.size.height)
        Direction.EAST -> obj.tile.addX( obj.size.width)
        else -> obj.tile.add(dir.delta)
    }
    return tiles.contains(tile)
}