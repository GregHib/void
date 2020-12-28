package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnPlayerMessage

class InterfaceOnPlayerMessageDecoder : MessageDecoder<InterfaceOnPlayerMessage>(1) {

    override fun decode(packet: PacketReader) = InterfaceOnPlayerMessage(
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(order = Endian.LITTLE),
        packet.readShort(),
        packet.readBoolean(Modifier.SUBTRACT),
        packet.readShort(Modifier.ADD, Endian.LITTLE)
    )

}