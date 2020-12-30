package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.packet.PacketReader

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(context: ChannelHandlerContext, packet: PacketReader) {}
}