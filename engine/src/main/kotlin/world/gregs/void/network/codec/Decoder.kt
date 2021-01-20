package world.gregs.void.network.codec

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(context: ChannelHandlerContext, packet: Reader) {}
}