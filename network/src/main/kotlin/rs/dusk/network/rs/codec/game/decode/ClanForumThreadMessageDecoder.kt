package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLAN_FORUM_THREAD
import rs.dusk.network.rs.codec.game.decode.message.ClanForumThreadMessage

@PacketMetaData(opcodes = [CLAN_FORUM_THREAD], length = VARIABLE_LENGTH_BYTE)
class ClanForumThreadMessageDecoder : GameMessageDecoder<ClanForumThreadMessage>() {

    override fun decode(packet: PacketReader) = ClanForumThreadMessage(packet.readString())

}