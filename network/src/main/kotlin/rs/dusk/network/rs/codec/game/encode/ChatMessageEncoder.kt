package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CHAT
import rs.dusk.network.rs.codec.game.encode.message.ChatMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class ChatMessageEncoder : GameMessageEncoder<ChatMessage>() {

    override fun encode(builder: PacketWriter, msg: ChatMessage) {
        val (type, tile, text, name, formatted) = msg
        var mask = 0
        if (name != null) {
            mask = mask or 0x1
            if (formatted != null && name != formatted) {
                mask = mask or 0x2
            }
        }
        builder.apply {
            writeOpcode(CHAT, PacketType.BYTE)
            writeSmart(type)
            writeInt(tile)
            writeByte(mask)
            if (name != null) {
                writeString(name)
                if (mask and 0x2 == 0x2) {
                    writeString(formatted)
                }
            }
            writeString(text)
        }
    }
}