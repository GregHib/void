package world.gregs.void.world.interact.dialogue.event

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class StringEntered(override val player: Player, val value: String) : PlayerEvent() {
    companion object : EventCompanion<StringEntered>
}