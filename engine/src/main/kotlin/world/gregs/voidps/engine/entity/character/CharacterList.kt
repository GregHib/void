package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.client.update.task.viewport.spiral
import world.gregs.voidps.engine.map.ChunkMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.TileMap
import world.gregs.voidps.engine.map.chunk.Chunk

abstract class CharacterList<C : Character>(
    capacity: Int,
    private val tiles: TileMap<C> = TileMap(capacity),
    private val chunk: ChunkMap<C> = ChunkMap(capacity),
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate {
    private val chunks = mutableMapOf<Chunk, Int>()

    private val indices = Int2ObjectOpenHashMap<C>()

    override fun add(element: C): Boolean {
        indices[element.index] = element
        tiles.add(element.tile, element)
        chunk.add(element.tile.chunk, element)
        increment(element.tile.chunk)
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        tiles.remove(element.tile, element)
        chunk.remove(element.tile.chunk, element)
        return delegate.remove(element)
    }

    fun removeIndex(element: C) {
        decrement(element.tile.chunk)
        indices.remove(element.index)
    }

    operator fun get(tile: Tile): Set<C> = tiles[tile] ?: emptySet()

    operator fun get(chunk: Chunk): Set<C> = this.chunk[chunk] ?: emptySet()

    fun indexed(index: Int): C? = indices[index]

    fun update(from: Tile, to: Tile, element: C) {
        tiles.remove(from, element)
        tiles.add(to, element)
        if (from.chunk != to.chunk) {
            decrement(from.chunk)
            increment(to.chunk)
            chunk.remove(from.chunk, element)
            chunk.add(to.chunk, element)
        }
    }

    override fun clear() {
        tiles.clear()
        delegate.clear()
        chunks.clear()
    }

    private fun increment(chunk: Chunk) {
        for (c in chunk.spiral(2)) {
            chunks[c] = count(c) + 1
        }
    }

    private fun decrement(chunk: Chunk) {
        for (c in chunk.spiral(2)) {
            val count = count(c) - 1
            if (count < 1) {
                chunks.remove(c)
            } else {
                chunks[c] = count
            }
        }
    }

    fun count(chunk: Chunk) = chunks.getOrDefault(chunk, 0)
}