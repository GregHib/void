package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.map.Distance

/**
 * Keeps track of characters in a [radius] and their [state]
 */
interface CharacterTrackingSet<C : Character> : Iterable<C> {

    val localMax: Int
    val radius: Int
    var total: Int
    val state: ViewState

    fun remove(index: Int) = state.removing(index)

    fun local(index: Int) = state.local(index) || state.removing(index)

    fun add(index: Int) = state.adding(index)

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun start(self: C?)

    /**
     * Updates [locals] by adding and removing all by [state]
     */
    fun update(characters: CharacterList<C>)

    /**
     * Tracks changes of all entities in a [set]
     */
    fun track(set: IntArrayList, self: Int): Boolean {
        var index: Int
        for (i in set.indices) {
            index = set.getInt(i)
            if (total >= localMax) {
                return false
            }
            track(index, index == self)
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
                track(entity.index, entity.index == self?.index)
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
    fun track(entity: Int, self: Boolean)
}
