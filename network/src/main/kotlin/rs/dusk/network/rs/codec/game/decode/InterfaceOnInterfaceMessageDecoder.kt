package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ITEM_ON_ITEM
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnInterfaceMessage

@PacketMetaData(opcodes = [ITEM_ON_ITEM], length = 16)
class InterfaceOnInterfaceMessageDecoder : GameMessageDecoder<InterfaceOnInterfaceMessage>() {

    override fun decode(packet: PacketReader) = InterfaceOnInterfaceMessage(
        packet.readInt(order = Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD),
        packet.readShort(order = Endian.LITTLE)
    )

}