package world.gregs.void.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.buffer.DataType
import world.gregs.void.buffer.read.Reader
import world.gregs.void.network.codec.Decoder

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