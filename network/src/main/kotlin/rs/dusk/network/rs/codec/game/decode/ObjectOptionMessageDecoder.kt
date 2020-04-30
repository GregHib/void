package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_1
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_2
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_3
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_4
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_5
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_OPTION_6
import rs.dusk.network.rs.codec.game.decode.message.ObjectOptionMessage

@PacketMetaData(
    opcodes = [OBJECT_OPTION_1, OBJECT_OPTION_2, OBJECT_OPTION_3, OBJECT_OPTION_4, OBJECT_OPTION_5, OBJECT_OPTION_6],
    length = 7
)
class ObjectOptionMessageDecoder : GameMessageDecoder<ObjectOptionMessage>() {

    override fun decode(packet: PacketReader): ObjectOptionMessage {
        val run = packet.readBoolean(Modifier.ADD)
        val x = packet.readShort(Modifier.ADD)
        val id = packet.readUnsigned(DataType.SHORT, Modifier.ADD, Endian.LITTLE).toInt()
        val y = packet.readShort(order = Endian.LITTLE)
        val option = opcodes!!.indexOf(packet.opcode) + 1
        return ObjectOptionMessage(id, x, y, run, option)
    }

}