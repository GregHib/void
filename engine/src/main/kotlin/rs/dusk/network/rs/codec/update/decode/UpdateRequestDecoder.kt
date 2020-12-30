package rs.dusk.network.rs.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import rs.dusk.buffer.DataType
import rs.dusk.core.network.codec.message.Decoder
import rs.dusk.core.network.codec.packet.PacketReader

class UpdateRequestDecoder(private val priority: Boolean) : Decoder(3) {

    override fun decode(context: ChannelHandlerContext, packet: PacketReader) {
        val hash = packet.readUnsigned(DataType.MEDIUM)
        handler?.updateRequest(
            context = context,
            indexId = (hash shr 16).toInt(),
            archiveId = (hash and 0xffff).toInt(),
            priority = priority
        )
    }

}