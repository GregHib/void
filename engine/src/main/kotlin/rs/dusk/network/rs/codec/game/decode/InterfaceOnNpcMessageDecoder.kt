package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnNpcMessage

class InterfaceOnNpcMessageDecoder : MessageDecoder<InterfaceOnNpcMessage>(11) {

    override fun decode(packet: PacketReader) = InterfaceOnNpcMessage(
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readBoolean()
    )

}