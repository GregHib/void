package rs.dusk.engine.view

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.model.Tile
import rs.dusk.engine.view.ViewportTask.Companion.VIEW_RADIUS
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class EntityTrackingSet<T : Indexed>(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS,
    override var total: Int = 0,
    override val add: LinkedHashSet<T> = LinkedHashSet(),
    override val remove: MutableSet<T> = mutableSetOf(),
    override val current: MutableSet<T> = TreeSet(),// Ordered locals
    override val local: MutableSet<T> = mutableSetOf()// Duplicate of current for O(1) lookup
) : TrackingSet<T> {

    override fun prep(self: T?) {
        remove.addAll(current)
        total = 0
        if (self != null) {
            track(self, null)
        }
    }

    override fun update() {
        current.removeAll(remove)
        current.addAll(add)
        local.removeAll(remove)
        local.addAll(add)
        remove.clear()
        add.clear()
        total = current.size
    }

    override fun add(self: T) {
        current.add(self)
        local.add(self)
    }

    override fun track(set: Set<T?>, self: T?): Boolean {
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

    override fun track(set: Set<T?>, self: T?, x: Int, y: Int): Boolean {
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

    override fun clear() {
        add.clear()
        remove.clear()
        current.clear()
        local.clear()
        total = 0
    }

    /**
     * Tracking is done by adding all current entities for removal
     * and deleting visible ones, leaving entities
     * which have just been removed in the removal set.
     *
     * Note: [prep] must be called before use so [remove] is full with [current] visible entities
     */
    private fun track(entity: T, self: T?) {
        val visible = remove.remove(entity)
        if (visible) {
            total++
        } else if (self == null || entity != self) {
            if (add.size < tickMax) {
                add.add(entity)
                total++
            }
        }
    }

    companion object {
        private fun withinView(x: Int, y: Int, x2: Int, y2: Int, radius: Int): Boolean {
            return abs(x - x2) <= radius && abs(y - y2) <= radius
        }
    }
}
