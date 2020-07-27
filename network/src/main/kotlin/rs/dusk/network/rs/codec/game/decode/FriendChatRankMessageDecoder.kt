package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.io.Modifier
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.RANK_FRIEND_CHAT
import rs.dusk.network.rs.codec.game.decode.message.FriendChatRankMessage

@PacketMetaData(opcodes = [RANK_FRIEND_CHAT], length = PacketType.VARIABLE_LENGTH_BYTE)
class FriendChatRankMessageDecoder : GameMessageDecoder<FriendChatRankMessage>() {

    override fun decode(packet: PacketReader)
            = FriendChatRankMessage(packet.readString(), packet.readByte(Modifier.INVERSE))

}