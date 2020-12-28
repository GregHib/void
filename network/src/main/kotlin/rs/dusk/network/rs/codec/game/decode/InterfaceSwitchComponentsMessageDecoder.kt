package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.InterfaceSwitchComponentsMessage

class InterfaceSwitchComponentsMessageDecoder : MessageDecoder<InterfaceSwitchComponentsMessage>(16) {

    override fun decode(packet: PacketReader) = InterfaceSwitchComponentsMessage(
        packet.readShort(),
        packet.readShort(order = Endian.LITTLE),
        packet.readShort(Modifier.ADD),
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE)
    )

}