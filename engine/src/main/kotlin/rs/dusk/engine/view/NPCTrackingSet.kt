package rs.dusk.engine.view

import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.teleport
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.view.ViewportTask.Companion.VIEW_RADIUS
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class NPCTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS,
    override var total: Int = 0,
    override val add: LinkedHashSet<NPC> = LinkedHashSet(),
    override val remove: MutableSet<NPC> = mutableSetOf(),
    override val current: MutableSet<NPC> = TreeSet(),// Ordered locals
    override val local: MutableSet<NPC> = mutableSetOf(),// TODO remove
    override val lastSeen: MutableMap<NPC, Tile> = mutableMapOf()
) : TrackingSet<NPC> {

    override fun prep(self: NPC?) {
        remove.addAll(current)
        total = 0
        if (self != null) {
            track(self, null)
        }
    }

    override fun update() {
        remove.forEach {
            current.remove(it)
            local.remove(it)
        }
        add.forEach {
            local.add(it)
            current.add(it)
        }
        remove.clear()
        add.clear()
        total = current.size
    }

    override fun add(self: NPC) {
        current.add(self)
        local.add(self)
    }

    override fun track(set: Set<NPC?>, self: NPC?): Boolean {
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

    override fun track(set: Set<NPC?>, self: NPC?, x: Int, y: Int): Boolean {
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
     * Tracking is done by adding all entities to removal and deleting visible ones,
     * leaving entities which have just been removed in the removal set.
     *
     * Note: [prep] must be called beforehand so [remove] is full with [current] visible entities
     */
    private fun track(entity: NPC, self: NPC?) {
        val visible = !entity.teleport && remove.remove(entity)
        if (visible) {
            total++
        } else if (add.size < tickMax) {
            add.add(entity)
            total++
        }
    }

    companion object {
        private fun withinView(x: Int, y: Int, x2: Int, y2: Int, radius: Int): Boolean {
            return abs(x - x2) <= radius && abs(y - y2) <= radius
        }
    }
}
