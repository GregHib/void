package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CHAT_TYPE
import rs.dusk.network.rs.codec.game.decode.message.ChatTypeMessage

@PacketMetaData(opcodes = [CHAT_TYPE], length = 1)
class ChatTypeMessageDecoder : GameMessageDecoder<ChatTypeMessage>() {

    override fun decode(packet: PacketReader) = ChatTypeMessage(packet.readUnsignedByte())

}