package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.Event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
abstract class PlayerEvent : Event<Unit>() {
    abstract val player: Player
}