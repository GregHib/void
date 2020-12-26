package rs.dusk.tools.map.obj

import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Distance.euclidean
import rs.dusk.engine.map.Tile

val ladderOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    if (opt == "climb down" && target.opt == "climb up") {
        1.0
    } else if (opt == "climb up" && target.opt == "climb down") {
        1.0
    } else if (opt == "climb up" && target.obj.def.isTrapDoor()) {
        0.6
    } else {
        0.0
    }
}

private fun ObjectDefinition.isTrapDoor(): Boolean {
    val name = name.replace(" ", "").toLowerCase()
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
        Direction.NORTH -> obj.tile.add(y = obj.size.height)
        Direction.EAST -> obj.tile.add(x = obj.size.width)
        else -> obj.tile.add(dir.delta)
    }
    return tiles.contains(tile)
}

private fun getShortestDist(tiles1: Set<Tile>, tiles2: Set<Tile>): Double {
    var shortest = Double.MAX_VALUE
    tiles1.forEach { t1 ->
        tiles2.forEach { t2 ->
            val dist = euclidean(t1, t2)
            if (dist < shortest) {
                shortest = dist
            }
        }
    }
    return shortest
}