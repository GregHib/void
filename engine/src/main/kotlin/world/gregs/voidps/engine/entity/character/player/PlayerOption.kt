package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.EventCompanion

data class PlayerOption(override val player: Player, val target: Player, val option: String, val optionId: Int) : PlayerEvent() {
    companion object : EventCompanion<PlayerOption>
}