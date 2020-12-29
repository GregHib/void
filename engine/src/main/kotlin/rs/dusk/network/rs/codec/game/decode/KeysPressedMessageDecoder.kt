package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.BYTE

class KeysPressedMessageDecoder : MessageDecoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.buffer.hasRemaining()) {
            keys.add(packet.readUnsignedByte() to packet.readUnsignedShort())
        }
        handler?.keysPressed(context, keys)
    }

}