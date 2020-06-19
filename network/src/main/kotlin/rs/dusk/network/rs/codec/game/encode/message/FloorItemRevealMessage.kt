package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 * @param offset The tile offset from the [ChunkMessage] sent
 * @param id Item id
 * @param amount Item stack size
 * @param owner Client index if matches client's local index then item won't be displayed
 */
data class FloorItemRevealMessage(
    val offset: Int,
    val id: Int,
    val amount: Int,
    val owner: Int
) : Message