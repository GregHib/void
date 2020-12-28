package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.FloorItemOptionMessage

class FloorItemOptionMessageDecoder(private val index: Int) : GameMessageDecoder<FloorItemOptionMessage>(7) {

    override fun decode(packet: PacketReader) = FloorItemOptionMessage(
        packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt(),
        packet.readBoolean(),
        packet.readShort(),
        packet.readShort(order = Endian.LITTLE),
        index
    )

}