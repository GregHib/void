package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REMOVE_FRIEND
import rs.dusk.network.rs.codec.game.decode.message.FriendListRemoveMessage

@PacketMetaData(opcodes = [REMOVE_FRIEND], length = PacketType.VARIABLE_LENGTH_BYTE)
class FriendListRemoveMessageDecoder : GameMessageDecoder<FriendListRemoveMessage>() {

    override fun decode(packet: PacketReader) = FriendListRemoveMessage(packet.readString())

}