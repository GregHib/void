package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.client.compress
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A freeform [message] sent nearby from [source].
 */
data class ClanChatMessage(
    val source: Player,
    val effects: Int,
    val message: String
) : Event {
    val compressed = message.compress()
}