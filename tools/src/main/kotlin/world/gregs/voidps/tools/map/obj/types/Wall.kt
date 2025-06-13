package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

val wallOptions: ObjectIdentificationContext.(Tile) -> Double = { _ ->
    if (opt == "climb down" || opt == "climb up") {
        1.0
    } else {
        0.0
    }
}

val isOppositeTile: ObjectIdentificationContext.(Tile) -> Double = { target ->
    when {
        hasOppositeTile(obj, availableTiles, target) -> 0.4
        hasVerticalTile(obj, availableTiles, target) -> 0.3
        else -> 0.0
    }
}

val isPopulatedLevel: ObjectIdentificationContext.(Tile) -> Double = { target ->
    val collisions: Collisions = get()
    if (obj.tile.level == target.level) {
        (availableTiles.contains(target) && obj.reachableFrom(target)).toDouble()
    } else {
        target.toCuboid(4).any { collisions[it.x, it.y, it.level] != 0 }.toDouble()
    }
}

private fun hasOppositeTile(obj1: GameObject, tiles1: Set<Tile>, target: Tile): Boolean {
    for (dir in Direction.cardinal) {
        if (check(obj1, tiles1, dir) && target == obj1.tile.add(dir.inverse().delta)) {
            return true
        }
    }
    return false
}

private fun hasVerticalTile(obj1: GameObject, tiles1: Set<Tile>, target: Tile): Boolean {
    for (dir in Direction.values) {
        if (check(obj1, tiles1, dir) && target == obj1.tile.add(dir.inverse().delta)) {
            return true
        }
    }
    return false
}

private fun check(obj: GameObject, tiles: Set<Tile>, dir: Direction): Boolean {
    val tile = when (dir) {
        Direction.NORTH -> obj.tile.addY(obj.height)
        Direction.EAST -> obj.tile.addX(obj.width)
        else -> obj.tile.add(dir.delta)
    }
    return tiles.contains(tile)
}

private fun GameObject.reachableFrom(tile: Tile): Boolean {
    val collisions: Collisions = get()
    return false // interactTarget.reached(tile, Size.ONE) && !collisions.check(tile.x, tile.y, tile.level, 0x100)// BLOCKED
}

fun Boolean.toDouble() = if (this) 1.0 else 0.0
