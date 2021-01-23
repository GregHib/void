package world.gregs.voidps.world.interact.dialogue.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class StringEntered(override val player: Player, val value: String) : PlayerEvent() {
    companion object : EventCompanion<StringEntered>
}