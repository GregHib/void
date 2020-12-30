package rs.dusk.network.rs.codec.game.encode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.write.writeByte
import rs.dusk.buffer.write.writeShort
import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.core.network.codec.packet.PacketSize
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_CUSTOMISE

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectCustomiseEncoder : Encoder(OBJECT_CUSTOMISE, PacketSize.BYTE) {

    /**
     * Note: Populated arrays must be exact same size as originals
     * @param tile The tile offset from the chunk update send
     * @param id Object id
     * @param type Object type
     * @param modelIds Replacement model ids
     * @param colours Replacement colours
     * @param textureColours Replacement texture colours
     * @param clear Clear previous customisations
     */
    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        type: Int,
        modelIds: IntArray? = null,
        colours: IntArray? = null,
        textureColours: IntArray? = null,
        clear: Boolean = false
    ) = player.send(getLength(modelIds, colours, textureColours)) {
        writeByte(type, type = Modifier.ADD)
        var flag = 0
        if (clear) {
            flag = flag or 0x1
        }
        if (modelIds != null) {
            flag = flag or 0x2
        }
        if (colours != null) {
            flag = flag or 0x4
        }
        if (textureColours != null) {
            flag = flag or 0x8
        }
        writeByte(flag)
        writeByte(tile, type = Modifier.SUBTRACT)
        writeShort(id, order = Endian.LITTLE)
        modelIds?.forEach { modelId ->
            writeShort(modelId)
        }
        colours?.forEach { colour ->
            writeShort(colour)
        }
        textureColours?.forEach { textureColour ->
            writeShort(textureColour)
        }
    }

    private fun getLength(modelIds: IntArray?, colours: IntArray?, textureColours: IntArray?): Int {
        var count = 5
        count += (modelIds?.size ?: 0) * 2
        count += (colours?.size ?: 0) * 2
        count += (textureColours?.size ?: 0) * 2
        return count
    }
}