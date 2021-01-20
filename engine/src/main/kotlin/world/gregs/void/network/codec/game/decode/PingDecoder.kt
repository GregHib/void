package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class PingDecoder : Decoder(0) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.ping(context)
    }

}