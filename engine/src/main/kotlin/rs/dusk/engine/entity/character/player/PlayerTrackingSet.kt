package rs.dusk.engine.entity.character.player

import rs.dusk.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import rs.dusk.engine.entity.character.CharacterTrackingSet
import rs.dusk.engine.map.Tile
import java.util.*
import kotlin.collections.LinkedHashSet

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
class PlayerTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS,
    override val add: LinkedHashSet<Player> = LinkedHashSet(),
    override val remove: MutableSet<Player> = mutableSetOf(),
    override val current: MutableSet<Player> = TreeSet(),// Ordered locals
    val local: MutableSet<Player> = mutableSetOf(),// Duplicate of current for O(1) lookup
    val lastSeen: MutableMap<Player, Tile> = mutableMapOf()
) : CharacterTrackingSet<Player> {

    override var total: Int = 0

    override fun start(self: Player?) {
        remove.addAll(current)
        total = 0
        if (self != null) {
            track(self, null)
        }
    }

    override fun finish() {
        remove.forEach {
            lastSeen[it] = it.movement.trailingTile
        }
    }

    override fun update() {
        remove.forEach {
            current.remove(it)
            local.remove(it)
            lastSeen[it] = it.tile
        }
        add.forEach {
            local.add(it)
            current.add(it)
            lastSeen[it] = it.tile
        }
        remove.clear()
        add.clear()
        total = current.size
    }

    override fun add(self: Player) {
        current.add(self)
        local.add(self)
    }

    override fun clear() {
        add.clear()
        remove.clear()
        current.clear()
        local.clear()
        total = 0
    }

    override fun refresh(self: Player?) {
        add.addAll(current)
        current.clear()
        total = 0
        if (self != null) {
            add.remove(self)
            current.add(self)
            total = 1
        }
    }

    override fun track(entity: Player, self: Player?) {
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
}
