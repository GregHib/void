package rs.dusk.tools.map.obj

import rs.dusk.ai.*
import rs.dusk.cache.definition.data.ObjectDefinition
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.definition.ObjectDefinitions
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Distance
import rs.dusk.engine.map.Tile
import rs.dusk.utility.get

val ladderDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    Distance.euclidean(obj.tile, target.obj.tile).scale(0.0, 20.0).inverse().logistic()
}

val ladderOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val targetOpt = getTargetOption(target)
    ((opt == "climb down" && targetOpt == "climb up")
            || (opt == "climb up" && targetOpt == "climb down")).toDouble()
}

private fun getTargetOption(target: GameObjectOption): String {
    if (target.opt == "open") {
        when (target.obj.def.name.toLowerCase()) {
            "trapdoor" -> {
                val def = resolveObject(target.obj)
                return def.name.replace("-", " ").toLowerCase()
            }
            "manhole" -> return "climb down"
        }
    }
    return target.opt
}

private fun resolveObject(obj: GameObject): ObjectDefinition {
    return get<ObjectDefinitions>().get(obj.def.getOrNull("open") as? Int ?: return obj.def)
}

val interactionScore: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when {
        hasOppositeTile(obj, availableTiles, target.obj, target.tiles) -> 1.0
        hasVerticalTile(obj, availableTiles, target.obj, target.tiles) -> 0.9
        else -> getShortestDist(availableTiles, target.tiles)
            .scale(0.0, 10.0)
            .inverse()
            .linear(slope = 1.3)
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
            val dist = Distance.euclidean(t1, t2)
            if (dist < shortest) {
                shortest = dist
            }
        }
    }
    return shortest
}