package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Entity

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
interface TrackingSet<T : Entity> {

    val maximum: Int
    val radius: Int
    val total: Int
    val add: Set<T>
    val remove: Set<T>
    val current: Set<T>
    val local: Set<T>

    /**
     * Moves all entities to the removal list
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
    fun track(set: Set<T?>): Boolean

    /**
     * Tracks changes of entities in the [set] within view of [x], [y]
     */
    fun track(set: Set<T?>, x: Int, y: Int): Boolean

    /**
     * Clear everything in set
     */
    fun clear()

}
