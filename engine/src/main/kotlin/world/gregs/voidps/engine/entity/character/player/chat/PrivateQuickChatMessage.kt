package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A direct quick-chat message sent from [source].
 */
data class PrivateQuickChatMessage(
    val source: Player,
    val file: Int,
    val message: String,
    val data: ByteArray
) : Event