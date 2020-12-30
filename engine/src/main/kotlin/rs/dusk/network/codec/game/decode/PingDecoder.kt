package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Decoder
import rs.dusk.network.packet.PacketReader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class PingDecoder : Decoder(0) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.ping(context)
    }

}