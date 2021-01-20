package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder
import world.gregs.void.network.packet.PacketSize.BYTE

class ConsoleCommandDecoder : Decoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        handler?.consoleCommand(
            context,
            packet.readString()
        )
    }

}