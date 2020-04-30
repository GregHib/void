package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_1
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_2
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_3
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_4
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_5
import rs.dusk.network.rs.codec.game.GameOpcodes.FLOOR_ITEM_OPTION_6
import rs.dusk.network.rs.codec.game.decode.message.FloorItemOptionMessage

@PacketMetaData(
    opcodes = [FLOOR_ITEM_OPTION_1, FLOOR_ITEM_OPTION_2, FLOOR_ITEM_OPTION_3, FLOOR_ITEM_OPTION_4, FLOOR_ITEM_OPTION_5, FLOOR_ITEM_OPTION_6],
    length = 7
)

class FloorItemOptionMessageDecoder : GameMessageDecoder<FloorItemOptionMessage>() {

    override fun decode(packet: PacketReader) = FloorItemOptionMessage(
        packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt(),
        packet.readBoolean(),
        packet.readShort(),
        packet.readShort(order = Endian.LITTLE),
        opcodes!!.indexOf(packet.opcode) + 1
    )

}