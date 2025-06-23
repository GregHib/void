package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.tools.map.obj.GameObjectOption
import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext
import world.gregs.voidps.type.Distance.euclidean
import world.gregs.voidps.type.Distance.getNearest
import world.gregs.voidps.type.Distance.levenshtein
import world.gregs.voidps.type.Tile
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
    val difX = abs(obj.width - it.obj.width)
    val difY = abs(obj.height - it.obj.height)
    (difX + difY)
        .toDouble()
        .scale(0.0, 8.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}

/**
 * Interesting if ids are very close but if they're not close, no big deal either
 */
val differenceBetweenIds: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    abs(obj.def.id - target.obj.def.id)
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
fun Double.cosine(steepness: Double = 0.5, offset: Double = 0.0): Double = 1 - cos(this * PI * steepness) + offset

/**
 * Distance between objects taking size into account
 */
val objectDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val dist = getDistance(obj.tile, obj.width, obj.height, target.obj)
    if (onSurface(obj.tile) && inDungeon(target.obj.tile)) {
        max(dist, getDistance(obj.tile.addY(DUNGEON_DIFFERENCE), obj.width, obj.height, target.obj))
    } else if (inDungeon(obj.tile) && onSurface(target.obj.tile)) {
        max(dist, getDistance(obj.tile.minus(y = DUNGEON_DIFFERENCE), obj.width, obj.height, target.obj))
    } else {
        dist
    }
}

private const val DUNGEON_DIFFERENCE = 6400
private fun onSurface(tile: Tile) = tile.y < DUNGEON_DIFFERENCE
private fun inDungeon(tile: Tile) = tile.y > DUNGEON_DIFFERENCE

private fun getDistance(tile: Tile, width: Int, height: Int, target: GameObject): Double {
    val nearest = getNearest(tile, width, height, target.tile)
    val nearestTarget = target.nearestTo(tile)
    return euclidean(nearest, nearestTarget, level = nearest.level == 3 || nearest.x != nearestTarget.x || nearest.y != nearestTarget.y)
        .scale(0.0, 5.0)
        .inverse()
        .logistic(midpoint = -0.25)
}

/**
 * Note: normalized
 * @param steepness 0..1
 * @param midpoint -1..1
 */
fun Double.logistic(steepness: Double = 1.0, midpoint: Double = 0.0): Double = 1 / (1 + E.pow(-steepness * (4 * E * (this - midpoint) - (2 * E))))

fun Double.inverse() = 1.0 - this

fun Double.scale(min: Double, max: Double): Double = (coerceIn(min, max) - min) / (max - min)
