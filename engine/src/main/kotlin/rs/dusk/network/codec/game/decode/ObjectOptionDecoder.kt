package rs.dusk.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.DataType
import rs.dusk.buffer.Endian
import rs.dusk.buffer.Modifier
import rs.dusk.buffer.read.Reader
import rs.dusk.network.codec.Decoder

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