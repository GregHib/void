package rs.dusk.network.rs.codec.game.decode

import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.ObjectOptionMessage

class ObjectOptionMessageDecoder(private val index: Int) : MessageDecoder<ObjectOptionMessage>(7) {

    override fun decode(packet: PacketReader): ObjectOptionMessage {
        val run = packet.readBoolean(Modifier.ADD)
        val x = packet.readShort(Modifier.ADD)
        val id = packet.readUnsigned(DataType.SHORT, Modifier.ADD, Endian.LITTLE).toInt()
        val y = packet.readShort(order = Endian.LITTLE)
        val option = index + 1
        return ObjectOptionMessage(id, x, y, run, option)
    }

}