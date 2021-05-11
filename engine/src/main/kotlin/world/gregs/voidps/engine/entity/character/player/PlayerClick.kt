package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.Event

/**
 * Player click before the attempt to walk within interact distance
 */
data class PlayerClick(val target: Player, val option: String) : Event {
    var cancel = false
}