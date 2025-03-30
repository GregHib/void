package world.gregs.voidps.engine.entity.character

abstract class CharacterList<C : Character>(
    private val delegate: MutableList<C> = mutableListOf()
) : MutableList<C> by delegate, CharacterSearch<C> {

    abstract val indexArray: Array<C?>
    private var indexer = 1

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

    internal fun index(): Int? {
        if (indexer < indexArray.size) {
            return indexer++
        }
        for (i in 1 until indexArray.size) {
            if (indexArray[i] == null) {
                return i
            }
        }
        return null
    }

    fun removeIndex(element: C) {
        indexArray[element.index] = null
    }

    fun indexed(index: Int): C? = indexArray[index]

    override fun clear() {
        indexArray.fill(null)
        delegate.clear()
    }
}