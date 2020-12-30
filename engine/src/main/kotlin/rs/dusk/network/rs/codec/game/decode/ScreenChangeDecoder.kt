package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class ScreenChangeDecoder : Decoder(6) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.changeScreen(
            context = context,
            displayMode = packet.readUnsignedByte(),
            width = packet.readUnsignedShort(),
            height = packet.readUnsignedShort(),
            antialiasLevel = packet.readUnsignedByte()
        )
    }

}