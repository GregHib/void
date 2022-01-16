package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.client.compress
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A direct freeform [message] sent from [source].
 */
data class PrivateChatMessage(
    val source: Player,
    val message: String
) : Event {
    val compressed = message.compress()
}