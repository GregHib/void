package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.BYTE

class ClanSettingsUpdateDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.updateClanSettings(
            context,
            packet.readShort(),
            packet.readString()
        )
    }

}