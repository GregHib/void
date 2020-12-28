package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class NPCOptionMessageDecoder(private val index: Int) : MessageDecoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.npcOption(
            context,
            packet.readBoolean(Modifier.ADD),
            packet.readShort(Modifier.ADD),
            index + 1
        )
    }

}