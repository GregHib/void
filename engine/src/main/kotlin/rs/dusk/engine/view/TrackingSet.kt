package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
interface TrackingSet<T : Indexed> {

    val maximum: Int
    val radius: Int
    val total: Int
    val add: Set<T>
    val remove: Set<T>
    val current: Set<T>
    val local: Set<T>

    /**
     * Moves all entities to the removal list
     * Note: `remove` will always be empty due to [update] from last tick
     */
    fun prep(self: T?)

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
    fun track(set: Set<T?>, self: T?): Boolean

    /**
     * Tracks changes of entities in the [set] within view of [x], [y]
     */
    fun track(set: Set<T?>, self: T?, x: Int, y: Int): Boolean

    /**
     * Clear everything in set
     */
    fun clear()

}
