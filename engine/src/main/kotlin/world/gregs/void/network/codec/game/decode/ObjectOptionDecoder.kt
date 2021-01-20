package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.DataType
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class ObjectOptionDecoder(private val index: Int) : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            run = packet.readBoolean(Modifier.ADD),
            x = packet.readShort(Modifier.ADD),
            objectId = packet.readUnsigned(DataType.SHORT, Modifier.ADD, Endian.LITTLE).toInt(),
            y = packet.readShort(order = Endian.LITTLE),
            option = index + 1
        )
    }

}