package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnFloorItemMessage

class InterfaceOnFloorItemMessageDecoder : MessageDecoder<InterfaceOnFloorItemMessage>(15) {

    override fun decode(packet: PacketReader) = InterfaceOnFloorItemMessage(
        packet.readShort(),
        packet.readShort(),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readBoolean(),
        packet.readShort(order = Endian.LITTLE)
    )

}