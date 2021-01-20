package world.gregs.void.world.interact.dialogue.event

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class IntEntered(override val player: Player, val value: Int) : PlayerEvent() {
    companion object : EventCompanion<IntEntered>
}