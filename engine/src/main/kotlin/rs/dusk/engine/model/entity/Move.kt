package rs.dusk.engine.model.entity

import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 05, 2020
 */
data class Move(val entity: Entity, val from: Tile, val to: Tile) : Event() {
    companion object : EventCompanion<Move>()
}