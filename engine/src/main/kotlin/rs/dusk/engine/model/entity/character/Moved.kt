package rs.dusk.engine.model.entity.character

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 05, 2020
 */
data class Moved(val entity: Entity, val from: Tile, val to: Tile) : Event<Unit>() {
    companion object : EventCompanion<Moved>
}