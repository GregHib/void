package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

class ScreenChangeDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.changeScreen(
            context = context,
            displayMode = packet.readUnsignedByte(),
            width = packet.readUnsignedShort(),
            height = packet.readUnsignedShort(),
            antialiasLevel = packet.readUnsignedByte()
        )
    }

}