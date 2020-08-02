package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_TEXT
import rs.dusk.network.rs.codec.game.encode.message.InterfaceTextMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 2, 2020
 */
class InterfaceTextMessageEncoder : GameMessageEncoder<InterfaceTextMessage>() {

    override fun encode(builder: PacketWriter, msg: InterfaceTextMessage) {
        val (id, component, text) = msg
        builder.apply {
            writeOpcode(INTERFACE_TEXT, PacketType.SHORT)
            writeInt(id shl 16 or component)
            writeString(text)
        }
    }
}