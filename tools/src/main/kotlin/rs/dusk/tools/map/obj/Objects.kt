package rs.dusk.tools.map.obj

import rs.dusk.ai.*
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Distance.euclidean
import rs.dusk.engine.map.Distance.getNearest
import rs.dusk.engine.map.Distance.levenshtein
import rs.dusk.engine.map.Tile
import kotlin.math.abs
import kotlin.math.max

/*
    Order of importance
    names > size > ids
 */

/**
 * Names with over 20 difference between names still returns 0.6
 */
val differenceBetweenNames: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    levenshtein(obj.def.name, target.obj.def.name)
        .toDouble()
        .scale(0.0, 20.0)
        .inverse()
        .cosine(steepness = 0.3, offset = 0.6)
}

val sizeDifference: ObjectIdentificationContext.(GameObjectOption) -> Double = {
    val difX = abs(obj.size.width - it.obj.size.width)
    val difY = abs(obj.size.height - it.obj.size.height)
    (difX + difY)
        .toDouble()
        .scale(0.0, 8.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}

/**
 * Interesting if id's are very close but if they're not close no big deal either
 */
val differenceBetweenIds: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    abs(obj.id - target.obj.id)
        .toDouble()
        .scale(0.0, 10.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}

/**
 * Distance between objects taking size into account
 */
val objectDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val dist = getDistance(obj.tile, obj.size, target.obj)
    if (onSurface(obj.tile) && inDungeon(target.obj.tile)) {
        max(dist, getDistance(obj.tile.add(y = dungeonDifference), obj.size, target.obj))
    } else if (inDungeon(obj.tile) && onSurface(target.obj.tile)) {
        max(dist, getDistance(obj.tile.minus(y = dungeonDifference), obj.size, target.obj))
    } else {
        dist
    }
}

private val dungeonDifference = 6400
private fun onSurface(tile: Tile) = tile.y < dungeonDifference
private fun inDungeon(tile: Tile) = tile.y > dungeonDifference

private fun getDistance(tile: Tile, size: Size, target: GameObject): Double {
    val nearest = getNearest(tile, size, target.tile)
    val nearestTarget = getNearest(target.tile, target.size, tile)
    return euclidean(nearest, nearestTarget)
        .scale(0.0, 5.0)
        .inverse()
        .logistic(midpoint = -0.25)
}