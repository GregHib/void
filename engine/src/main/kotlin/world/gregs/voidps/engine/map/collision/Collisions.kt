package world.gregs.voidps.engine.map.collision

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile

data class Collisions(val delegate: MutableMap<Int, Int> = mutableMapOf()) : MutableMap<Int, Int> by delegate {
    fun add(player: Player) {
        add(player.tile.x, player.tile.y, player.tile.plane, CollisionFlag.ENTITY)
    }
    fun add(npc: NPC) {
        for (x in 0 until npc.size.width) {
            for (y in 0 until npc.size.height) {
                add(npc.tile.x + x, npc.tile.y + y, npc.tile.plane, CollisionFlag.ENTITY)
            }
        }
    }
    fun remove(character: Character) {
        remove(character.tile.x, character.tile.y, character.tile.plane, CollisionFlag.ENTITY)
    }

    fun move(character: Character, from: Tile, to: Tile) {
        // No simple way of looking up if an npc is over a tile (incl size)
        // This means players can remove npcs collisions.
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                remove(from.x + x, from.y + y, from.plane, CollisionFlag.ENTITY)
            }
        }
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                add(to.x + x, to.y + y, to.plane, CollisionFlag.ENTITY)
            }
        }
    }
}

@Suppress("USELESS_CAST")
val collisionModule = module {
    single(createdAtStart = true) { GameObjectCollision(get()) }
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