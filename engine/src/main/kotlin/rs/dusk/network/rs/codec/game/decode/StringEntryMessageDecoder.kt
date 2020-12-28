package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE

class StringEntryMessageDecoder : MessageDecoder(VARIABLE_LENGTH_BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.stringEntered(
            context = context,
            text = packet.readString()
        )
    }

}