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
            context,
            packet.readBoolean(Modifier.INVERSE),
            packet.readShort(order = Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readInt(order = Endian.LITTLE),
            packet.readShort(Modifier.ADD, Endian.LITTLE),
            packet.readShort(order = Endian.LITTLE),
            packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt()
        )
    }

}