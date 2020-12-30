package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader
import rs.dusk.core.network.codec.packet.PacketSize.BYTE

class ReportAbuseDecoder : Decoder(BYTE) {

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