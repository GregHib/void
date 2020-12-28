package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.packet.access.PacketReader

abstract class MessageDecoder(val length: Int) {

    var handler: MessageHandler? = null

    open fun decode(context: ChannelHandlerContext, packet: PacketReader) {}
}