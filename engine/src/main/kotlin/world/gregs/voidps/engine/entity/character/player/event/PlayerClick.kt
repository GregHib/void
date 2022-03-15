package world.gregs.voidps.engine.entity.character.player.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent

/**
 * A player click before the attempt to walk within interact distance
 */
data class PlayerClick(val target: Player, val option: String) : CancellableEvent()