package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.view.ViewportTask.Companion.VIEW_RADIUS
import java.util.*
import kotlin.math.abs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class TrackingSet<T : Entity>(
    val maximum: Int,
    val radius: Int = VIEW_RADIUS,
    var total: Int = 0,
    val add: Deque<T> = LinkedList(),
    var remove: MutableSet<T> = mutableSetOf(),
    var current: MutableSet<T> = mutableSetOf()
) {
    /**
     * Moves all entities to the removal list by switching the sets
     */
    fun switch() {
        val temp = current
        current = remove
        remove = temp
        total = 0
    }

    /**
     * Tracks changes of all entities in a [set]
     */
    fun update(set: Set<T>): Boolean {
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
    fun update(set: Set<T>, x: Int, y: Int): Boolean {
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
     * Tracking is done by removing all current entities (switching sets)
     * and moving visible ones back into the current list, leaving entities
     * which have just been removed in the removal set.
     *
     * Note:
     *  Switch must be called before use
     *  Due to the switch `remove` is full with current visible entities
     *  Before the switch `remove` will always be empty from the last tick
     */
    private fun track(entity: T) {
        if (remove.remove(entity)) {
            current.add(entity)
        } else {
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
