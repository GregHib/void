package rs.dusk.engine.model.world.map.collision

import org.koin.dsl.module
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Collisions(val delegate: MutableMap<Tile, Int> = mutableMapOf()) :
    MutableMap<Tile, Int> by delegate

@Suppress("USELESS_CAST")
val collisionModule = module {
    single { Collisions() }
    single { CollisionReader(get()) }
}

fun Collisions.add(x: Int, y: Int, plane: Int, flag: Int) {
    val tile = Tile(x, y, plane)
    val value = get(tile) ?: 0
    this[tile] = value or flag
}

operator fun Collisions.set(x: Int, y: Int, plane: Int, flag: Int) {
    this[Tile(x, y, plane)] = flag
}

fun Collisions.remove(x: Int, y: Int, plane: Int, flag: Int) =
    add(x, y, plane, flag.inv())

operator fun Collisions.get(x: Int, y: Int, plane: Int) =
    this[Tile(x, y, plane)] ?: 0

fun Collisions.collides(x: Int, y: Int, plane: Int, flag: Int) =
    this[x, y, plane] and flag != 0