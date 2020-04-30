package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.KEY_TYPED
import rs.dusk.network.rs.codec.game.decode.message.KeysPressedMessage

@PacketMetaData(opcodes = [KEY_TYPED], length = PacketType.VARIABLE_LENGTH_BYTE)
class KeysPressedMessageDecoder : GameMessageDecoder<KeysPressedMessage>() {

    override fun decode(packet: PacketReader): KeysPressedMessage {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.buffer.hasRemaining()) {
            keys.add(Pair(packet.readUnsignedByte(), packet.readUnsignedShort()))
        }
        return KeysPressedMessage(keys)
    }

}