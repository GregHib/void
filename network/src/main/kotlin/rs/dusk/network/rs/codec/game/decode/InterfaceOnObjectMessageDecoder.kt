package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOnObjectMessage

class InterfaceOnObjectMessageDecoder : GameMessageDecoder<InterfaceOnObjectMessage>(15) {

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