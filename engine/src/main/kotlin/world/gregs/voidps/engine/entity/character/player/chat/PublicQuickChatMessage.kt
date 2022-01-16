package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A quick-chat message sent nearby from [source].
 */
data class PublicQuickChatMessage(
    val source: Player,
    val script: Int,
    val file: Int,
    val message: String,
    val data: ByteArray
) : Event