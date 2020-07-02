package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_PRE_FETCH
import rs.dusk.network.rs.codec.game.encode.message.ObjectPreloadMessage

/**
 * Preloads a object model
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
class ObjectPreloadMessageEncoder : GameMessageEncoder<ObjectPreloadMessage>() {

    override fun encode(builder: PacketWriter, msg: ObjectPreloadMessage) {
        val (id, modelType) = msg
        builder.apply {
            writeOpcode(OBJECT_PRE_FETCH)
            writeShort(id)
            writeByte(modelType)
        }
    }
}