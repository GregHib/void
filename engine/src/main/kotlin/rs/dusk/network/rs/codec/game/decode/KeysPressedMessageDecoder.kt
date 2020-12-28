package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE

class KeysPressedMessageDecoder : MessageDecoder(VARIABLE_LENGTH_BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.buffer.hasRemaining()) {
            keys.add(packet.readUnsignedByte() to packet.readUnsignedShort())
        }
        handler?.keysPressed(context, keys)
    }

}