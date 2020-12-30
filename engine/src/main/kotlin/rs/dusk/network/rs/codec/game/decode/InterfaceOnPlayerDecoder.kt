package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class InterfaceOnPlayerDecoder : Decoder(1) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceOnPlayer(
            context,
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(order = Endian.LITTLE),
            packet.readShort(),
            packet.readBoolean(Modifier.SUBTRACT),
            packet.readShort(Modifier.ADD, Endian.LITTLE)
        )
    }

}