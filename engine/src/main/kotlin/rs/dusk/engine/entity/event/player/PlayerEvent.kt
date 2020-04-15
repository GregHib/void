package rs.dusk.engine.entity.event.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class PlayerEvent : Event() {
    abstract val player: Player
}