package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PING_REPLY
import rs.dusk.network.rs.codec.game.decode.message.PingReplyMessage

@PacketMetaData(opcodes = [PING_REPLY], length = 8)
class PingReplyMessageDecoder : GameMessageDecoder<PingReplyMessage>() {

    override fun decode(packet: PacketReader) = PingReplyMessage(packet.readInt(), packet.readInt())

}