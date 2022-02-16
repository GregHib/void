package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.map.Distance

interface CharacterTrackingSet<T : Character> {

    val localMax: Int
    val radius: Int
    val total: Int

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun start(self: T?)

    /**
     * Updates [current] by adding all [addSelf] and removing all [remove]
     */
    fun update()

    /**
     * Tracks the clients own player
     */
    fun addSelf(self: T)

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
