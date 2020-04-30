package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLAN_CHAT_KICK
import rs.dusk.network.rs.codec.game.decode.message.ClanChatKickMessage

@PacketMetaData(opcodes = [CLAN_CHAT_KICK], length = VARIABLE_LENGTH_BYTE)
class ClanChatKickMessageDecoder : GameMessageDecoder<ClanChatKickMessage>() {

    override fun decode(packet: PacketReader) =
        ClanChatKickMessage(packet.readBoolean(), packet.readShort(), packet.readString())

}