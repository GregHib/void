package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

/**
 * A quick-chat message a player wants (but has yet) to say to everyone nearby.
 */
data class PublicQuickChat(
    val script: Int,
    val file: Int,
    val data: ByteArray
) : Event