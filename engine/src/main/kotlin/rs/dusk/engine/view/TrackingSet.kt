package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.view.ViewportTask.Companion.VIEW_RADIUS
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class TrackingSet<T : Entity>(
    val maximum: Int,
    val radius: Int = VIEW_RADIUS,
    var total: Int = 0,
    val add: LinkedHashSet<T> = LinkedHashSet(),
    val remove: MutableSet<T> = mutableSetOf(),
    val current: MutableSet<T> = TreeSet()
) {
    /**
     * Moves all entities to the removal list
     */
    fun prep() {
        remove.addAll(current)
        total = 0
    }

    /**
     * Updates [current] by adding all [add] and removing all [remove]
     */
    fun update() {
        current.removeAll(remove)
        current.addAll(add)
        remove.clear()
        add.clear()
        total = current.size
    }

    /**
     * Tracks changes of all entities in a [set]
     */
    fun track(set: Set<T>): Boolean {
        for (entity in set) {
            if (total >= maximum) {
                return false
            }
            track(entity)
        }
        return true
    }

    /**
     * Tracks changes of entities in the [set] within view of [x], [y]
     */
    fun track(set: Set<T>, x: Int, y: Int): Boolean {
        for (entity in set) {
            if (total >= maximum) {
                return false
            }
            if (withinView(entity.tile.x, entity.tile.y, x, y, radius)) {
                track(entity)
            }
        }
        return true
    }

    /**
     * Tracking is done by adding all current entities for removal
     * and deleting visible ones, leaving entities
     * which have just been removed in the removal set.
     *
     * Note:
     *  [prep] must be called before use
     *  Due to the prep `remove` is full with current visible entities
     *  Before the prep `remove` will always be empty from the last tick
     */
    private fun track(entity: T) {
        if (!remove.remove(entity)) {
            add.add(entity)
        }
        total++
    }

    fun clear() {
        add.clear()
        remove.clear()
        current.clear()
        total = 0
    }

    companion object {
        private fun withinView(x: Int, y: Int, x2: Int, y2: Int, radius: Int): Boolean {
            return abs(x - x2) <= radius && abs(y - y2) <= radius
        }
    }
}
