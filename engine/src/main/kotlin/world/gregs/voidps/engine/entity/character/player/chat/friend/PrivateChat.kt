package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.event.Event

/**
 * A freeform [message] a player wants (but has yet) to send directly to a [friend].
 */
data class PrivateChat(
    val friend: String,
    val message: String
) : Event