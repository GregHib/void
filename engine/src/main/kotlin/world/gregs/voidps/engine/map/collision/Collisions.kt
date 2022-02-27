package world.gregs.voidps.engine.map.collision

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.strategy.*
import world.gregs.voidps.engine.map.region.RegionPlane

class Collisions(
    val data: Array<IntArray?> = arrayOfNulls(256 * 256 * 4)
) {

    operator fun get(x: Int, y: Int, plane: Int): Int {
        val region = RegionPlane.getId(x / 64, y / 64, plane)
        if (data[region] == null) {
            return CollisionFlag.BLOCKED
        }
        return data[region]!![index(x, y)]
    }

    operator fun set(x: Int, y: Int, plane: Int, flag: Int) {
        val region = RegionPlane.getId(x / 64, y / 64, plane)
        if (region == -1) {
            return
        }
        if (data[region] == null) {
            data[region] = IntArray(4096)
        }
        data[region]!![index(x, y)] = flag
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

    /**
     * Note:
     *  Only suitable for copying of tile collisions, object definitions flags would require transformations
     *  Could accidentally copy collisions of characters active in [from]
     */
    fun copy(from: Chunk, to: Chunk, rotation: Int) {
        val array = data[from.regionPlane.id]?.clone() ?: return
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                val toX = if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x
                val toY = if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y
                val value = CollisionFlag.rotate(array[index(from.tile.x + x, from.tile.y + y)], rotation)
                set(to.tile.x + toX, to.tile.y + toY, to.plane, value)
            }
        }
    }

    fun clear(region: RegionPlane) {
        data[region.id]?.fill(0)
    }

    private fun index(x: Int, y: Int) = x.rem(64) * 64 + y.rem(64)

    private fun entity(character: Character): Int = if (character is Player) CollisionFlag.PLAYER else (CollisionFlag.NPC or if (character["solid", false]) CollisionFlag.BLOCKED else 0)

}

@Suppress("USELESS_CAST")
val collisionModule = module {
    single(createdAtStart = true) { GameObjectCollision(get()) }
    single { Collisions() }
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