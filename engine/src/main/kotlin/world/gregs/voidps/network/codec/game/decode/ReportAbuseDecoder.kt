package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class ReportAbuseDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.reportAbuse(
            context = context,
            name = packet.readString(),
            type = packet.readByte(),
            integer = packet.readByte(),
            string = packet.readString()
        )
    }

}