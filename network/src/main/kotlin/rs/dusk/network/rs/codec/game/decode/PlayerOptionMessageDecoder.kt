package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_1
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_10
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_2
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_3
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_4
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_5
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_6
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_7
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_8
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_OPTION_9
import rs.dusk.network.rs.codec.game.decode.message.PlayerOptionMessage

@PacketMetaData(
    opcodes = [PLAYER_OPTION_1, PLAYER_OPTION_2, PLAYER_OPTION_3, PLAYER_OPTION_4, PLAYER_OPTION_5, PLAYER_OPTION_6, PLAYER_OPTION_7, PLAYER_OPTION_8, PLAYER_OPTION_9, PLAYER_OPTION_10],
    length = 3
)
class PlayerOptionMessageDecoder : GameMessageDecoder<PlayerOptionMessage>() {

    override fun decode(packet: PacketReader): PlayerOptionMessage {
        packet.readByte()//0
        return PlayerOptionMessage(packet.readShort(), opcodes!!.indexOf(packet.opcode) + 1)
    }

}