package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class PingDecoder : Decoder(0) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.ping(context)
    }

}