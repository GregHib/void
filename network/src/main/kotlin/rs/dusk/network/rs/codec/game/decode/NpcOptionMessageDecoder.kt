package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_1
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_2
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_3
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_4
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_5
import rs.dusk.network.rs.codec.game.GameOpcodes.NPC_OPTION_6
import rs.dusk.network.rs.codec.game.decode.message.NpcOptionMessage

@PacketMetaData(
    opcodes = [NPC_OPTION_1, NPC_OPTION_2, NPC_OPTION_3, NPC_OPTION_4, NPC_OPTION_5, NPC_OPTION_6],
    length = 3
)
class NpcOptionMessageDecoder : GameMessageDecoder<NpcOptionMessage>() {

    override fun decode(packet: PacketReader) = NpcOptionMessage(
        packet.readBoolean(Modifier.ADD),
        packet.readShort(Modifier.ADD),
        opcodes!!.indexOf(packet.opcode) + 1
    )

}