package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.map.Distance

interface CharacterTrackingSet<T : Character> {

    val localMax: Int
    val radius: Int
    var total: Int

    val locals: IntArray
    val state: ViewState
    var lastIndex: Int
    var addCount: Int
    val indices: IntRange
        get() = 0 until lastIndex

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun start(self: T?) {
        for (i in indices) {
            val index = locals[i]
            if (index != self?.index) {
                state.setRemoving(index)
                total--
            }
        }
    }

    /**
     * Updates [current] by adding all [addSelf] and removing all [remove]
     */
    fun update(characters: CharacterList<T>) {
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
    fun track(set: Set<T?>, self: T?): Boolean {
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
    fun track(set: Iterable<T>, self: T?, x: Int, y: Int): Boolean {
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
     * Note: [start] must be called beforehand so [remove] is full of [current] visible entities
     */
    fun track(entity: T, self: T?)
}
