package world.gregs.voidps.network.codec

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(context: ChannelHandlerContext, packet: Reader) {}
}