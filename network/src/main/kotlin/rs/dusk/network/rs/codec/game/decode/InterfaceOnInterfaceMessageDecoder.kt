package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnInterfaceMessage

class InterfaceOnInterfaceMessageDecoder : MessageDecoder<InterfaceOnInterfaceMessage>(16) {

    override fun decode(packet: PacketReader) = InterfaceOnInterfaceMessage(
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort(order = Endian.LITTLE)
    )

}