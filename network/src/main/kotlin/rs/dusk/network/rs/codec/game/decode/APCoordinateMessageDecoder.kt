package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.AP_COORD_T
import rs.dusk.network.rs.codec.game.decode.message.APCoordinateMessage

@PacketMetaData(opcodes = [AP_COORD_T], length = 12)
class APCoordinateMessageDecoder : GameMessageDecoder<APCoordinateMessage>() {

    override fun decode(packet: PacketReader) = APCoordinateMessage(
        packet.readShort(Modifier.ADD),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort()
    )

}