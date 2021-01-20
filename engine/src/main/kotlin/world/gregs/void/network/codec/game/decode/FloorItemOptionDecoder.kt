package world.gregs.void.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.DataType
import world.gregs.void.buffer.Endian
import world.gregs.void.buffer.Modifier
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

class FloorItemOptionDecoder(private val index: Int) : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.floorItemOption(
            context,
            packet.readUnsigned(DataType.SHORT, Modifier.ADD).toInt(),
            packet.readBoolean(),
            packet.readShort(),
            packet.readShort(order = Endian.LITTLE),
            index
        )
    }

}