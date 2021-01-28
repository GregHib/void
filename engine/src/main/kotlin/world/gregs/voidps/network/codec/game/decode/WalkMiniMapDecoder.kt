package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader

class WalkMiniMapDecoder : Decoder(18) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.minimapWalk(
            context = context,
            y = packet.readShort(order = Endian.LITTLE),
            running = packet.readBoolean(Modifier.ADD),
            x = packet.readShort(Modifier.ADD)
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