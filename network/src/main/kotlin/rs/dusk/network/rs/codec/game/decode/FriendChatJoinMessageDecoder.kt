package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.JOIN_FRIEND_CHAT
import rs.dusk.network.rs.codec.game.decode.message.FriendChatJoinMessage

@PacketMetaData(opcodes = [JOIN_FRIEND_CHAT], length = PacketType.VARIABLE_LENGTH_BYTE)
class FriendChatJoinMessageDecoder : GameMessageDecoder<FriendChatJoinMessage>() {

    override fun decode(packet: PacketReader) = FriendChatJoinMessage(packet.readString())

}