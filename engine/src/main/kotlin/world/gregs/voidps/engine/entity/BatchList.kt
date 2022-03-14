package world.gregs.voidps.engine.entity

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.proj.Projectiles
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

interface BatchList<T : Entity> {

    val chunks: MutableMap<Int, MutableList<T>>

    fun add(entity: T) = chunks.getOrPut(entity.tile.chunk.id) { mutableListOf() }.add(entity)

    fun remove(entity: T): Boolean {
        val tile = chunks[entity.tile.chunk.id] ?: return false
        return tile.remove(entity)
    }

    fun clear(chunk: Chunk) {
        chunks.remove(chunk.id)
    }

    operator fun get(tile: Tile): List<T> = get(tile.chunk).filter { it.tile == tile }

    operator fun get(chunk: Chunk): List<T> = chunks[chunk.id] ?: emptyList()
}

const val MAX_PLAYERS = 0x800// 2048
const val MAX_NPCS = 0x8000// 32768

val entityListModule = module {
    single { NPCs(get(), get(), get(), get()) }
    single { Players() }
    single { Objects() }
    single { FloorItems(get(), get(), get(), get(), get()) }
    single { Projectiles() }
    single { Graphics() }
    single { Sounds() }
}