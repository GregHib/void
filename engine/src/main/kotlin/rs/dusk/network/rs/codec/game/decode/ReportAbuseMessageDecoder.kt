package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.BYTE

class ReportAbuseMessageDecoder : MessageDecoder(BYTE) {

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