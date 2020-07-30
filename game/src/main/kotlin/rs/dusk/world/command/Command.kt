package rs.dusk.world.command

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 01, 2020
 */
data class Command(override val player: Player, val prefix: String, val content: String) : PlayerEvent() {
    companion object : EventCompanion<Command>
}