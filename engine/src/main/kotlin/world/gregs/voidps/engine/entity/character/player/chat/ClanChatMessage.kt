package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A freeform [message] sent nearby from [source].
 */
data class ClanChatMessage(
    val source: Player,
    val effects: Int,
    val message: String,
    val compressed: ByteArray
) : Event {
    constructor(source: Player, effects: Int, message: String, huffman: Huffman) : this(source, effects, message, huffman.compress(message))
}