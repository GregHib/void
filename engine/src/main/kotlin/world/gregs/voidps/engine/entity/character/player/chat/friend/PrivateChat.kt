package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A freeform [message] a player wants (but has yet) to send directly to a [friend].
 */
data class PrivateChat(
    val friend: String,
    val message: String
) : Event {

    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "private_chat"
        else -> null
    }
}