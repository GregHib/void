package rs.dusk.engine.model.entity.character.player

import rs.dusk.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class PlayerEvent : Event<Unit>() {
    abstract val player: Player
}