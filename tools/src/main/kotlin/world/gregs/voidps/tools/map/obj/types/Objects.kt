package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Distance.euclidean
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Distance.levenshtein
import world.gregs.voidps.engine.map.Tile
import kotlin.math.*

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
    abs(obj.intId - target.obj.intId)
        .toDouble()
        .scale(0.0, 10.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}

/**
 * @param slope 0..100
 * @param offset -1..1
 */
fun Double.linear(slope: Double = 1.0, offset: Double = 0.0) = (this / slope) - offset

/**
 * @param steepness 0..1
 * @param offset -1..1
 */
fun Double.cosine(steepness: Double = 0.5, offset: Double = 0.0): Double {
    return 1 - cos(this * PI * steepness) + offset
}

/**
 * Distance between objects taking size into account
 */
val objectDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val dist = getDistance(obj.tile, obj.size, target.obj)
    if (onSurface(obj.tile) && inDungeon(target.obj.tile)) {
        max(dist, getDistance(obj.tile.addY(dungeonDifference), obj.size, target.obj))
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
    return euclidean(nearest, nearestTarget, plane = nearest.plane == 3 || nearest.x != nearestTarget.x || nearest.y != nearestTarget.y)
        .scale(0.0, 5.0)
        .inverse()
        .logistic(midpoint = -0.25)
}

/**
 * Note: normalized
 * @param steepness 0..1
 * @param midpoint -1..1
 */
fun Double.logistic(steepness: Double = 1.0, midpoint: Double = 0.0): Double {
    return 1 / (1 + E.pow(-steepness * (4 * E * (this - midpoint) - (2 * E))))
}

fun Double.inverse() = 1.0 - this

fun Double.scale(min: Double, max: Double): Double {
    return (coerceIn(min, max) - min) / (max - min)
}