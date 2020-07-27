package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.ADD_FRIEND
import rs.dusk.network.rs.codec.game.decode.message.FriendListAddMessage

@PacketMetaData(opcodes = [ADD_FRIEND], length = PacketType.VARIABLE_LENGTH_BYTE)
class FriendListAddMessageDecoder : GameMessageDecoder<FriendListAddMessage>() {

    override fun decode(packet: PacketReader) = FriendListAddMessage(packet.readString())

}