package rs.dusk.world.interact.dialogue

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class IntEntered(override val player: Player, val value: Int) : PlayerEvent() {
    companion object : EventCompanion<IntEntered>
}