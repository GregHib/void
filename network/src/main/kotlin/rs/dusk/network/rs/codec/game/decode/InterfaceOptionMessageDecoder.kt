package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOptionMessage

class InterfaceOptionMessageDecoder(private val index: Int) : GameMessageDecoder<InterfaceOptionMessage>(8) {

    override fun decode(packet: PacketReader) = InterfaceOptionMessage(
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(),
        index
    )

}