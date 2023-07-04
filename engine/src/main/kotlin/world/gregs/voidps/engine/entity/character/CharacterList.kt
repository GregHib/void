package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.RegionPlane

abstract class CharacterList<C : Character>(
    capacity: Int,
    private val region: CharacterMap = CharacterMap(),
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate {

    abstract val indexArray: Array<C?>
    val indexer = IndexAllocator(capacity)

    override fun add(element: C): Boolean {
        if (indexArray[element.index] != null) {
            return false
        }
        index(element)
        region.add(element.tile.regionPlane, element)
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        region.remove(element.tile.regionPlane, element)
        return delegate.remove(element)
    }

    fun index(element: C) {
        indexArray[element.index] = element
    }

    fun removeIndex(element: C) {
        indexArray[element.index] = null
    }

    operator fun get(tile: Tile): List<C> {
        return get(tile.regionPlane).filter { it.tile == tile }
    }

    operator fun get(chunk: Chunk): List<C> {
        return get(chunk.regionPlane).filter { it.tile.chunk == chunk }
    }

    operator fun get(region: RegionPlane): List<C> {
        val list = mutableListOf<C>()
        for (index in this.region[region] ?: return list) {
            list.add(indexed(index) ?: continue)
        }
        return list
    }

    fun getDirect(region: RegionPlane): List<Int>? = this.region[region]

    fun indexed(index: Int): C? = indexArray[index]

    fun update(from: Tile, to: Tile, element: C) {
        if (from.regionPlane != to.regionPlane) {
            region.remove(from.regionPlane, element)
            region.add(to.regionPlane, element)
        }
    }

    override fun clear() {
        indexArray.fill(null)
        delegate.clear()
    }
}