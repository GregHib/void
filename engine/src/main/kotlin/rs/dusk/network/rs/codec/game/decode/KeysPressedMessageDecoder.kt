package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.decode.message.KeysPressedMessage

class KeysPressedMessageDecoder : MessageDecoder<KeysPressedMessage>(VARIABLE_LENGTH_BYTE) {

    override fun decode(packet: PacketReader): KeysPressedMessage {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.buffer.hasRemaining()) {
            keys.add(Pair(packet.readUnsignedByte(), packet.readUnsignedShort()))
        }
        return KeysPressedMessage(keys)
    }

}