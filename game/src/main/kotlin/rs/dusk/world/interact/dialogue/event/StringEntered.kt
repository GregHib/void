package rs.dusk.world.interact.dialogue.event

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class StringEntered(override val player: Player, val value: String) : PlayerEvent() {
    companion object : EventCompanion<StringEntered>
}