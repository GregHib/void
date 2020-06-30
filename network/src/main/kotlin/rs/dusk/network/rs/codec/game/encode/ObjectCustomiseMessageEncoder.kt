package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_CUSTOMISE
import rs.dusk.network.rs.codec.game.encode.message.ObjectCustomiseMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class ObjectCustomiseMessageEncoder : GameMessageEncoder<ObjectCustomiseMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectCustomiseMessage) {
        val (tile, id, type, modelIds, colours, textureColours, clear) = msg
        builder.apply {
            writeOpcode(OBJECT_CUSTOMISE, PacketType.BYTE)
            writeByte(type, type = Modifier.ADD)
            var flag = 0
            if(clear) {
                flag = flag or 0x1
            }
            if(modelIds != null) {
                flag = flag or 0x2
            }
            if(colours != null) {
                flag = flag or 0x4
            }
            if(textureColours != null) {
                flag = flag or 0x8
            }
            println(msg)
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
    }
}