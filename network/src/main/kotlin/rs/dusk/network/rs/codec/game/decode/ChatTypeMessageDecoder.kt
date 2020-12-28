package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.ChatTypeMessage

class ChatTypeMessageDecoder : GameMessageDecoder<ChatTypeMessage>(1) {

    override fun decode(packet: PacketReader) = ChatTypeMessage(packet.readUnsignedByte())

}