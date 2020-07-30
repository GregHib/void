package rs.dusk.world.command

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 01, 2020
 */
data class Command(override val player: Player, val prefix: String, val content: String) : PlayerEvent() {
    companion object : EventCompanion<Command>
}