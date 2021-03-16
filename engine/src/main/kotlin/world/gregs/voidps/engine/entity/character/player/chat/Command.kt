package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

/**
 * @author GregHib <greg@gregs.world>
 * @since May 01, 2020
 */
data class Command(override val player: Player, val prefix: String, val content: String) : PlayerEvent() {
    companion object : EventCompanion<Command>
}