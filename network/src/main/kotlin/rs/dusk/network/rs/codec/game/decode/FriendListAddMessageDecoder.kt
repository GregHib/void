package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.FriendListAddMessage

class FriendListAddMessageDecoder : GameMessageDecoder<FriendListAddMessage>(VARIABLE_LENGTH_BYTE) {

    override fun decode(packet: PacketReader) = FriendListAddMessage(packet.readString())

}