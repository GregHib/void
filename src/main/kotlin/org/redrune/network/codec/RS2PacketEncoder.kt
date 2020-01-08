package org.redrune.network.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.packet.struct.PacketHeader
import org.redrune.network.packet.struct.OutgoingPacket

@ChannelHandler.Sharable
class RS2PacketEncoder : MessageToByteEncoder<OutgoingPacket>() {

    override fun encode(ctx: ChannelHandlerContext, packet: OutgoingPacket, out: ByteBuf) {

        try { // the session
            val session: NetworkSession = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
            // the encoded response
            val response: ByteBuf
            if (packet.isRaw()) {
                response = packet.buffer
            } else { // the length of the packet
                val length: Int = packet.buffer.readableBytes()
                // create the buffer
                response = Unpooled.buffer(length + 3)
                // the id of the packet
                val opcode: Int = packet.opcode
                // the packet header
                val header = packet.header
                // if there was no cipher
                if (opcode >= 128) {
                    response.writeByte((opcode shr 8) + 128)
                }
                response.writeByte(opcode)
                if (header == PacketHeader.BYTE) {
                    check(length <= 255) {
                        // Stack overflow.
                        "Could not send a packet with $length bytes within 8 bits."
                    }
                    response.writeByte(length)
                } else if (header == PacketHeader.SHORT) {
                    check(length <= 65535) {
                        // Stack overflow.
                        "Could not send a packet with $length bytes within 16 bits."
                    }
                    response.writeShort(length)
                }
                response.writeBytes(packet.buffer)
            }
            out.writeBytes(response)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

}