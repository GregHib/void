package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.codec.packet.access.PacketSize.BYTE

class ConsoleCommandMessageDecoder : MessageDecoder(BYTE) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        handler?.consoleCommand(
            context,
            packet.readString()
        )
    }

}