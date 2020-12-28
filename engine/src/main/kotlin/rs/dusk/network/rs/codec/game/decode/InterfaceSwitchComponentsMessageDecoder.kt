package rs.dusk.network.rs.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader

class InterfaceSwitchComponentsMessageDecoder : MessageDecoder(16) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        handler?.interfaceSwitch(
            context,
            packet.readShort(),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(Modifier.ADD),
            packet.readInt(order = Endian.MIDDLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(Modifier.INVERSE, Endian.MIDDLE)
        )
    }

}