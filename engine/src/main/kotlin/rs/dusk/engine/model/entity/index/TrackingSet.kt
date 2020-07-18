package rs.dusk.engine.model.entity.index

import kotlin.math.abs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
interface TrackingSet<T : Character> {

    val maximum: Int
    val radius: Int
    val total: Int
    val add: Set<T>
    val remove: Set<T>
    val current: Set<T>

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun start(self: T?)

    fun finish()

    /**
     * Updates [current] by adding all [add] and removing all [remove]
     */
    fun update()

    /**
     * Tracks the clients own player
     */
    fun add(self: T)

    /**
     * Tracks changes of all entities in a [set]
     */
    fun track(set: Set<T?>, self: T?): Boolean {
        for (entity in set) {
            if (entity == null) {
                continue
            }
            if (total >= maximum) {
                return false
            }
            track(entity, self)
        }
        return true
    }

    /**
     * Tracks changes of entities in the [set] within view of [x], [y]
     */
    fun track(set: Set<T?>, self: T?, x: Int, y: Int): Boolean {
        for (entity in set) {
            if (entity == null) {
                continue
            }
            if (total >= maximum) {
                return false
            }
            if (withinView(entity.tile.x, entity.tile.y, x, y, radius)) {
                track(entity, self)
            }
        }
        return true
    }

    /**
     * Tracking is done by adding all entities to removal and deleting visible ones,
     * leaving entities which have just been removed in the removal set.
     *
     * Note: [start] must be called beforehand so [remove] is full with [current] visible entities
     */
    fun track(entity: T, self: T?)

    /**
     * Clear everything in set
     */
    fun clear()

    companion object {
        private fun withinView(x: Int, y: Int, x2: Int, y2: Int, radius: Int): Boolean {
            return abs(x - x2) <= radius && abs(y - y2) <= radius
        }
    }
}
