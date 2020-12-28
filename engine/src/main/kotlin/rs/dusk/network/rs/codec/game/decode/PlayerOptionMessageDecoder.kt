package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class PlayerOptionMessageDecoder(private val index: Int) : MessageDecoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        packet.readByte()//0
        handler?.playerOption(
            context,
            index = packet.readShort(),
            option = index + 1
        )
    }

}