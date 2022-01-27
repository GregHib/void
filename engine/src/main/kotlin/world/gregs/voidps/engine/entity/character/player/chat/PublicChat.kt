package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

/**
 * A freeform [text] a player wants (but has yet) to say to everyone nearby.
 */
data class PublicChat(
    val text: String,
    val effects: Int
) : Event