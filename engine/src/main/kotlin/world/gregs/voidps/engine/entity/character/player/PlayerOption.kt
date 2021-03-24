package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.Event

data class PlayerOption(val target: Player, val option: String, val optionId: Int) : Event