package world.gregs.voidps.engine.map.collision

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.*
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.Xteas

class Collisions(
    val regions: IntArray,
    val data: Array<Array<IntArray>> = Array(4) { Array(1528) { IntArray(4096) } }
) {

    operator fun get(x: Int, y: Int, plane: Int): Int {
        return data[plane][regions[Region.getId(x / 64, y / 64)]][x.rem(64) * 64 + y.rem(64)]
    }

    operator fun set(x: Int, y: Int, plane: Int, flag: Int) {
        data[plane][regions[Region.getId(x / 64, y / 64)]][x.rem(64) * 64 + y.rem(64)] = flag
    }

    fun add(x: Int, y: Int, plane: Int, flag: Int) {
        set(x, y, plane, get(x, y, plane) or flag)
    }

    fun remove(x: Int, y: Int, plane: Int, flag: Int) {
        set(x, y, plane, get(x, y, plane) and flag.inv())
    }

    fun check(x: Int, y: Int, plane: Int, flag: Int): Boolean {
        return get(x, y, plane) and flag != 0
    }

    fun check(tile: Tile, flag: Int) = check(tile.x, tile.y, tile.plane, flag)

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
    single {
        val keys = get<Xteas>().delegate.keys
        val array = IntArray(keys.maxOf { it } + 1)
        for((index, region) in keys.withIndex()) {
            array[region] = index
        }
        Collisions(array) }
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