package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder
import world.gregs.void.network.packet.PacketSize.BYTE

class KeysPressedDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.buffer.hasRemaining()) {
            keys.add(packet.readUnsignedByte() to packet.readUnsignedShort())
        }
        handler?.keysPressed(context, keys)
    }

}