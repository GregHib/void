package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class NPCOptionDecoder(private val index: Int) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.npcOption(
            context,
            packet.readBoolean(Modifier.ADD),
            packet.readShort(Modifier.ADD),
            index + 1
        )
    }

}