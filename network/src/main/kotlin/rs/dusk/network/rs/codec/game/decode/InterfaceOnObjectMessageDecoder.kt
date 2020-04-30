package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ITEM_ON_OBJECT
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnObjectMessage

@PacketMetaData(opcodes = [ITEM_ON_OBJECT], length = 15)
class InterfaceOnObjectMessageDecoder : GameMessageDecoder<InterfaceOnObjectMessage>() {

    override fun decode(packet: PacketReader) = InterfaceOnObjectMessage(
        packet.readBoolean(Modifier.INVERSE),
        packet.readShort(order = Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readInt(order = Endian.LITTLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(order = Endian.LITTLE),
        packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt()
    )

}