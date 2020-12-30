package rs.dusk.network.codec

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.packet.PacketReader

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(context: ChannelHandlerContext, packet: PacketReader) {}
}