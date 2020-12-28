package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE

class ReportAbuseMessageDecoder : MessageDecoder(VARIABLE_LENGTH_BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.reportAbuse(
            context = context,
            name = packet.readString(),
            type = packet.readByte(),
            integer = packet.readByte(),
            string = packet.readString()
        )
    }

}