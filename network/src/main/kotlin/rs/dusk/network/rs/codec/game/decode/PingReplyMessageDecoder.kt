package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.decode.message.PingReplyMessage

class PingReplyMessageDecoder : MessageDecoder<PingReplyMessage>(8) {

    override fun decode(packet: PacketReader) = PingReplyMessage(packet.readInt(), packet.readInt())

}