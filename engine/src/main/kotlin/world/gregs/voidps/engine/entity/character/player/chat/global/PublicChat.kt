package world.gregs.voidps.engine.entity.character.player.chat.global

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * A freeform [text] a player wants (but has yet) to say to everyone nearby.
 */
data class PublicChat(
    val text: String,
    val effects: Int
) : Event {

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "public_chat"
        else -> null
    }
}