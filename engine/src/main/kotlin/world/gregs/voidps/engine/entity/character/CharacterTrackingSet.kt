package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.map.Distance

/**
 * Keeps track of characters in a [radius] and their [state]
 */
interface CharacterTrackingSet<C : Character> : Iterable<C> {

    val localMax: Int
    val radius: Int
    var total: Int

    val locals: IntArray
    val state: ViewState
    var lastIndex: Int
    var addCount: Int
    val indices: IntRange
        get() = 0 until lastIndex

    fun remove(index: Int) = state.removing(index)

    fun local(index: Int) = state.local(index) || state.removing(index)

    fun add(index: Int) = state.adding(index)

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun start(self: C?) {
        var index: Int
        for (i in indices) {
            index = locals[i]
            if (index != self?.index) {
                state.setRemoving(index)
            }
        }
        total = if (self != null) 1 else 0
    }

    /**
     * Updates [locals] by adding and removing all by [state]
     */
    fun update(characters: CharacterList<C>) {
        lastIndex = 0
        for (index in 1 until characters.indexer.cap) {
            if (state.removing(index)) {
                state.setGlobal(index)
            } else if (state.adding(index) || state.local(index)) {
                state.setLocal(index)
                locals[lastIndex++] = index
            }
        }
        addCount = 0
        total = lastIndex
    }

    /**
     * Tracks changes of all entities in a [set]
     */
    fun track(set: Set<C?>, self: C?): Boolean {
        for (entity in set) {
            if (entity == null) {
                continue
            }
            if (total >= localMax) {
                return false
            }
            track(entity, self)
        }
        return true
    }

    /**
     * Tracks changes of entities in the [set] within view of [x], [y]
     */
    fun track(set: Iterable<C>, self: C?, x: Int, y: Int): Boolean {
        for (entity in set) {
            if (total >= localMax) {
                return false
            }
            if (Distance.within(entity.tile.x, entity.tile.y, x, y, radius)) {
                track(entity, self)
            }
        }
        return true
    }

    /**
     * Tracking is done by adding all entities to removal and deleting visible ones,
     * leaving entities which have just been removed in the removal set.
     *
     * Note: [start] must be called beforehand so [remove] is full of [locals] visible entities
     */
    fun track(entity: C, self: C?)

    fun iterator(list: CharacterList<C>): Iterator<C> {
        return object : Iterator<C> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < lastIndex
            }

            override fun next(): C {
                return list.indexed(locals[index++])!!
            }
        }
    }
}
