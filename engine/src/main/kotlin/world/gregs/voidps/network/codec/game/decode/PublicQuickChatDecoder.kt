package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class PublicQuickChatDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.publicQuickChat(
            context = context,
            script = packet.readByte(),
            file = packet.readUnsignedShort(),
            data = ByteArray(packet.readableBytes()).apply {
                packet.readBytes(this)
            }
        )
    }

}