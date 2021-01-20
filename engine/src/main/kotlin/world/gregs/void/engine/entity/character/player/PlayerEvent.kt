package world.gregs.void.engine.entity.character.player

import world.gregs.void.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class PlayerEvent : Event<Unit>() {
    abstract val player: Player
}