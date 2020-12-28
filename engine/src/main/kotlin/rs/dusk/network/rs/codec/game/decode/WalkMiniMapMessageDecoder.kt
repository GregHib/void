package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class WalkMiniMapMessageDecoder : MessageDecoder(18) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.minimapWalk(
            context = context,
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(Modifier.ADD, Endian.LITTLE),
            running = packet.readBoolean()
        )
        packet.readByte()//-1
        packet.readByte()//-1
        packet.readShort()//Rotation?
        packet.readByte()//57
        val minimapRotation = packet.readByte()
        val minimapZoom = packet.readByte()
        packet.readByte()//89
        packet.readShort()//X in region?
        packet.readShort()//Y in region?
        packet.readByte()//63
    }

}