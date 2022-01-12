package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.CancellableEvent

/**
 * Player click before the attempt to walk within interact distance
 */
data class PlayerClick(val target: Player, val option: String) : CancellableEvent()