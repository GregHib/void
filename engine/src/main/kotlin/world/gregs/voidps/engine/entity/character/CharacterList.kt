package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile

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
        region.add(element.tile.regionLevel, element)
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        region.remove(element.tile.regionLevel, element)
        return delegate.remove(element)
    }

    fun index(element: C) {
        indexArray[element.index] = element
    }

    fun removeIndex(element: C) {
        indexArray[element.index] = null
    }

    operator fun get(tile: Tile): List<C> {
        return get(tile.regionLevel).filter { it.tile == tile }
    }

    operator fun get(zone: Zone): List<C> {
        return get(zone.regionLevel).filter { it.tile.zone == zone }
    }

    operator fun get(region: RegionLevel): List<C> {
        val list = mutableListOf<C>()
        for (index in this.region[region] ?: return list) {
            list.add(indexed(index) ?: continue)
        }
        return list
    }

    fun getDirect(region: RegionLevel): List<Int>? = this.region[region]

    fun indexed(index: Int): C? = indexArray[index]

    fun update(from: Tile, to: Tile, element: C) {
        if (from.regionLevel != to.regionLevel) {
            region.remove(from.regionLevel, element)
            region.add(to.regionLevel, element)
        }
    }

    fun clear(region: RegionLevel) {
        for (index in this.region[region] ?: return) {
            val element = indexed(index) ?: continue
            delegate.remove(element)
            removeIndex(element)
            releaseIndex(element)
        }
    }

    fun releaseIndex(character: C) {
        if (character.index > 0) {
            indexer.release(character.index)
        }
    }

    override fun clear() {
        indexArray.fill(null)
        delegate.clear()
    }
}