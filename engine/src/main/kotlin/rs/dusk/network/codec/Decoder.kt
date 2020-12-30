package rs.dusk.network.codec

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(context: ChannelHandlerContext, packet: Reader) {}
}