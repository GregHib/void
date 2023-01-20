package world.gregs.voidps.engine.entity.character.player.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.SuspendableEvent

data class PlayerOption(val target: Player, val option: String, val optionId: Int) : SuspendableEvent()