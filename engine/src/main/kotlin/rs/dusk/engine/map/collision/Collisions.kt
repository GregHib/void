package rs.dusk.engine.map.collision

import org.koin.dsl.module
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Collisions(val delegate: MutableMap<Int, Int> = mutableMapOf()) :
    MutableMap<Int, Int> by delegate

@Suppress("USELESS_CAST")
val collisionModule = module {
    single { Collisions() }
    single { CollisionReader(get()) }
}

fun Collisions.add(x: Int, y: Int, plane: Int, flag: Int) {
    val tile = Tile.getId(x, y, plane)
    val value = get(tile) ?: 0
    this[tile] = value or flag
}

operator fun Collisions.set(x: Int, y: Int, plane: Int, flag: Int) {
    this[Tile.getId(x, y, plane)] = flag
}

fun Collisions.remove(x: Int, y: Int, plane: Int, flag: Int) {
    val tile = Tile.getId(x, y, plane)
    val value = get(tile) ?: 0
    this[tile] = value and flag.inv()
}

operator fun Collisions.get(x: Int, y: Int, plane: Int) =
    this[Tile.getId(x, y, plane)] ?: 0

fun Collisions.check(x: Int, y: Int, plane: Int, flag: Int) =
    this[x, y, plane] and flag != 0