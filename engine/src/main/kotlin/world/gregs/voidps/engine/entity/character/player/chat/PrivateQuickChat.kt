package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

/**
 * A quick-chat message a player wants (but has yet) to say directly to a [friend].
 */
data class PrivateQuickChat(
    val friend: String,
    val file: Int,
    val data: ByteArray
) : Event