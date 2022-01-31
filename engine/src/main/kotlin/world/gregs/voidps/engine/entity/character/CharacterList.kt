package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.client.update.task.viewport.spiral
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.TileMap
import world.gregs.voidps.engine.map.chunk.Chunk

abstract class CharacterList<C : Character>(
    capacity: Int,
    private val spatial: TileMap<C> = TileMap(capacity),
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate {
    private val chunks = mutableMapOf<Chunk, Int>()

    private val indices = Int2ObjectOpenHashMap<C>()

    override fun add(element: C): Boolean {
        indices[element.index] = element
        spatial[element.tile] = element
        increment(element.tile.chunk)
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        spatial.remove(element.tile, element)
        return delegate.remove(element)
    }

    fun removeIndex(element: C) {
        decrement(element.tile.chunk)
        indices.remove(element.index)
    }

    operator fun get(tile: Tile): Set<C> = spatial.get(tile)

    operator fun get(chunk: Chunk): List<C> = chunk.toCuboid().flatMap { get(it) }

    fun indexed(index: Int): C? = indices[index]

    fun update(from: Tile, to: Tile, element: C) {
        spatial.remove(from, element)
        spatial[to] = element
        if (from.chunk != to.chunk) {
            decrement(from.chunk)
            increment(to.chunk)
        }
    }

    override fun clear() {
        spatial.clear()
        delegate.clear()
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