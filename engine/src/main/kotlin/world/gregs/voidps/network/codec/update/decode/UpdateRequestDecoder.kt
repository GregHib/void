package world.gregs.voidps.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class UpdateRequestDecoder(private val priority: Boolean) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        val hash = packet.readUnsigned(DataType.MEDIUM)
        handler?.updateRequest(
            context = context,
            indexId = (hash shr 16).toInt(),
            archiveId = (hash and 0xffff).toInt(),
            priority = priority
        )
    }

}