package rs.dusk.engine.entity.character.move

import rs.dusk.engine.entity.Entity
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 05, 2020
 */
data class Moved(val entity: Entity, val from: Tile, val to: Tile) : Event<Unit>() {
    companion object : EventCompanion<Moved>
}