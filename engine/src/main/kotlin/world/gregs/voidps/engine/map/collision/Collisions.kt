package world.gregs.voidps.engine.map.collision

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.*

data class Collisions(val delegate: MutableMap<Int, Int> = Int2IntOpenHashMap()) : MutableMap<Int, Int> by delegate {

    fun add(char: Character) {
        for (x in 0 until char.size.width) {
            for (y in 0 until char.size.height) {
                add(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
            }
        }
    }

    fun remove(char: Character) {
        for (x in 0 until char.size.width) {
            for (y in 0 until char.size.height) {
                remove(char.tile.x + x, char.tile.y + y, char.tile.plane, entity(char))
            }
        }
    }

    fun move(character: Character, from: Tile, to: Tile) {
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                remove(from.x + x, from.y + y, from.plane, entity(character))
            }
        }
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                add(to.x + x, to.y + y, to.plane, entity(character))
            }
        }
    }

    private fun entity(character: Character): Int = if (character is Player) CollisionFlag.PLAYER else (CollisionFlag.NPC or if (character["solid", false]) CollisionFlag.BLOCKED else 0)
}

@Suppress("USELESS_CAST")
val collisionModule = module {
    single(createdAtStart = true) { GameObjectCollision(get()) }
    single { Collisions() }
    single { CollisionReader(get()) }
    single { CollisionStrategyProvider(get(), get(), get(), get(), get()) }
    single { ShoreCollision(get(), get(), get()) }
    single { WaterCollision(get()) }
    single { SkyCollision(get()) }
    single { CharacterCollision(get()) }
    single { LandCollision(get()) }
    single { IgnoredCollision(get(), get()) }
    single { NoCollision(get()) }
    single { RoofCollision(get(), get()) }
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

fun Collisions.check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)