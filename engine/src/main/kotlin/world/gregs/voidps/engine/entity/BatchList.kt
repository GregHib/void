package world.gregs.voidps.engine.entity

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