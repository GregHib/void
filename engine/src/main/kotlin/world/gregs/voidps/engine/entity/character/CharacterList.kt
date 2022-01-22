package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.TileMap
import world.gregs.voidps.engine.map.chunk.Chunk

abstract class CharacterList<C : Character>(
    capacity: Int,
    private val spatial: TileMap<C> = TileMap(capacity),
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate {

    private val indices = Int2ObjectOpenHashMap<C>()

    override fun add(element: C): Boolean {
        indices[element.index] = element
        spatial[element.tile] = element
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        indices.remove(element.index)
        spatial.remove(element.tile, element)
        return delegate.remove(element)
    }

    operator fun get(tile: Tile): Set<C> = spatial.get(tile)

    operator fun get(chunk: Chunk): List<C> = chunk.toCuboid().flatMap { get(it) }

    fun indexed(index: Int): C? = indices[index]

    fun update(from: Tile, to: Tile, element: C) {
        spatial.remove(from, element)
        spatial[to] = element
    }

    override fun clear() {
        spatial.clear()
        delegate.clear()
    }
}