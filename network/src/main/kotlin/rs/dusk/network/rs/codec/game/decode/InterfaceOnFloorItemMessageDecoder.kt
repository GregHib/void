package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_ON_FLOOR_ITEM
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnFloorItemMessage

@PacketMetaData(opcodes = [INTERFACE_ON_FLOOR_ITEM], length = 15)
class InterfaceOnFloorItemMessageDecoder : GameMessageDecoder<InterfaceOnFloorItemMessage>() {

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