package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class InterfaceOnObjectDecoder : Decoder(15) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.interfaceOnObject(
            context = context,
            y = packet.readShort(Modifier.ADD),
            slot = packet.readShort(Modifier.ADD, Endian.LITTLE),
            hash = packet.readInt(order = Endian.LITTLE),
            type = packet.readShort(Modifier.ADD),
            run = packet.readBoolean(Modifier.SUBTRACT),
            x = packet.readShort(order = Endian.LITTLE),
            id = packet.readUnsigned(DataType.SHORT, order = Endian.LITTLE).toInt()
        )
    }

}