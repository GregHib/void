package rs.dusk.engine.model.entity.player.command

import rs.dusk.engine.entity.event.player.PlayerEvent
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.EventCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 01, 2020
 */
data class Command(override val player: Player, val prefix: String, val content: String) : PlayerEvent() {
    companion object : EventCompanion<Command>()
}