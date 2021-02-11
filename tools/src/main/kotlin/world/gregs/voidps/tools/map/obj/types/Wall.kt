package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.area
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.collision.get
import world.gregs.voidps.utility.get

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

val isPopulatedPlane: ObjectIdentificationContext.(Tile) -> Double = { target ->
    val collisions: Collisions = get()
    if (obj.tile.plane == target.plane) {
        (availableTiles.contains(target) && obj.reachableFrom(target)).toDouble()
    } else {
        target.area(4).any { collisions[it.x, it.y, it.plane] != 0 }.toDouble()
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
        Direction.NORTH -> obj.tile.addY(obj.size.height)
        Direction.EAST -> obj.tile.addX(obj.size.width)
        else -> obj.tile.add(dir.delta)
    }
    return tiles.contains(tile)
}

private fun GameObject.reachableFrom(tile: Tile): Boolean {
    val collisions: Collisions = get()
    return interactTarget.reached(tile, Size.TILE) && !collisions.check(tile.x, tile.y, tile.plane, CollisionFlag.BLOCKED)
}