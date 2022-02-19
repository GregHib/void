package world.gregs.voidps.engine.map.collision

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.*
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.voidps.engine.map.region.Xteas

class Collisions(
    val regions: IntArray,
    val data: Array<IntArray?> = arrayOfNulls(0)
) {

    operator fun get(x: Int, y: Int, plane: Int): Int {
        val region = RegionPlane.getId(x / 64, y / 64, plane)
        val index = regions[region]
        if (index == -1 || data[index] == null) {
            return 0
        }
        return data[index]!![x.rem(64) * 64 + y.rem(64)]
    }

    operator fun set(x: Int, y: Int, plane: Int, flag: Int) {
        val region = RegionPlane.getId(x / 64, y / 64, plane)
        val index = regions[region]
        if (index == -1) {
            return
        }
        if (data[index] == null) {
            data[index] = IntArray(4096)
        }
        data[index]!![x.rem(64) * 64 + y.rem(64)] = flag
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

    companion object {
        operator fun invoke(xteas: Xteas): Collisions {
            return invoke(xteas.delegate.keys)
        }

        operator fun invoke(regions: Set<Int> = setOf(0)): Collisions {
            val array = IntArray(256 * 256 * 4) { -1 }
            var index = 0
            for (region in regions) {
                for (plane in 0 until 4) {
                    array[Region(region).toPlane(plane).id] = index++
                }
            }
            return Collisions(array, arrayOfNulls(index))
        }
    }

}

@Suppress("USELESS_CAST")
val collisionModule = module {
    single(createdAtStart = true) { GameObjectCollision(get()) }
    single { Collisions(get<Xteas>()) }
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