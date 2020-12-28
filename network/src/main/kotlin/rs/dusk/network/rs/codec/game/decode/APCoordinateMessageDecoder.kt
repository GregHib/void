package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.APCoordinateMessage

class APCoordinateMessageDecoder : GameMessageDecoder<APCoordinateMessage>(12) {

    override fun decode(packet: PacketReader) = APCoordinateMessage(
        packet.readShort(Modifier.ADD),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort()
    )

}