package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

abstract class CharacterList<C : Character>(
    capacity: Int,
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate {

    abstract val indexArray: Array<C?>
    val indexer = IndexAllocator(capacity)

    override fun add(element: C): Boolean {
        if (indexArray[element.index] != null) {
            return false
        }
        index(element)
        return delegate.add(element)
    }

    override fun remove(element: C): Boolean {
        return delegate.remove(element)
    }

    fun index(element: C) {
        indexArray[element.index] = element
    }

    fun removeIndex(element: C) {
        indexArray[element.index] = null
    }

    abstract operator fun get(tile: Tile): List<C>

    abstract operator fun get(zone: Zone): List<C>

    fun indexed(index: Int): C? = indexArray[index]

    fun releaseIndex(character: C) {
        if (character.index > 0) {
            indexer.release(character.index)
        }
    }

    override fun clear() {
        indexArray.fill(null)
        delegate.clear()
        indexer.clear()
    }
}