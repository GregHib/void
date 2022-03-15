package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A direct freeform [message] sent from [source].
 */
data class PrivateChatMessage(
    val source: Player,
    val message: String,
    val compressed: ByteArray
) : Event {
    constructor(source: Player, message: String, huffman: Huffman) : this(source, message, huffman.compress(message))
}