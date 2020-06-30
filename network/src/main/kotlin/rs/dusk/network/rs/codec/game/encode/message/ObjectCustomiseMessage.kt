package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Note: Populated arrays must be exact same size as originals
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 30, 2020
 * @param offset The tile offset from the [ChunkUpdateMessage] sent
 * @param id Object id
 * @param type Object type
 * @param modelIds Replacement model ids
 * @param colours Replacement colours
 * @param textureColours Replacement texture colours
 * @param clear Clear previous customisations
 */
data class ObjectCustomiseMessage(
    val offset: Int,
    val id: Int,
    val type: Int,
    val modelIds: IntArray? = null,
    val colours: IntArray? = null,
    val textureColours: IntArray? = null,
    val clear: Boolean = false
) : Message