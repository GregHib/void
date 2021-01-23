package world.gregs.voidps.world.interact.dialogue.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class IntEntered(override val player: Player, val value: Int) : PlayerEvent() {
    companion object : EventCompanion<IntEntered>
}