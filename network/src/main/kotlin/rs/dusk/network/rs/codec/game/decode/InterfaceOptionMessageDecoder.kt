package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_1
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_10
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_2
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_3
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_4
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_5
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_6
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_7
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_8
import rs.dusk.network.rs.codec.game.GameOpcodes.INTERFACE_OPTION_9
import rs.dusk.network.rs.codec.game.decode.message.InterfaceOptionMessage

@PacketMetaData(
    opcodes = [INTERFACE_OPTION_1, INTERFACE_OPTION_2, INTERFACE_OPTION_3, INTERFACE_OPTION_4, INTERFACE_OPTION_5, INTERFACE_OPTION_6, INTERFACE_OPTION_7, INTERFACE_OPTION_8, INTERFACE_OPTION_9, INTERFACE_OPTION_10],
    length = 8
)
class InterfaceOptionMessageDecoder : GameMessageDecoder<InterfaceOptionMessage>() {

    override fun decode(packet: PacketReader) = InterfaceOptionMessage(
        packet.readInt(Modifier.INVERSE, Endian.MIDDLE),
        packet.readShort(Modifier.ADD, Endian.LITTLE),
        packet.readShort(),
        opcodes!!.indexOf(packet.opcode) + 1
    )

}