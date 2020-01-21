package org.redrune.network.codec.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.NetworkSession
import org.redrune.network.packet.Packet
import org.redrune.network.packet.struct.PacketHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-18
 */
@ChannelHandler.Sharable
class RS2PacketEncoder : MessageToByteEncoder<Packet>() {

    override fun encode(ctx: ChannelHandlerContext, packet: Packet, out: ByteBuf) {
        println("RS2PacketEncoder.encode")

        try { // the session
            val session: NetworkSession = ctx.channel().attr(NetworkSession.SESSION_KEY).get()
            // the encoded response
            val response: ByteBuf
            if (packet.isRaw()) {
                response = packet.buffer
            } else {
                val length: Int = packet.buffer.readableBytes()
                response = Unpooled.buffer(length + 3)
                val opcode: Int = packet.opcode
                val header = packet.header
                val cipher = session.isaacPair?.encodingRandom
                if (cipher != null) {
                    if (opcode >= 128) {
                        response.writeByte(((opcode shr 8) + 128) + cipher.nextInt())
                        response.writeByte(opcode + cipher.nextInt())
                    } else {
                        response.writeByte(opcode + cipher.nextInt())
                    }
                } else {
                    writeSmart(opcode, response)
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

    private fun writeSmart(value: Int, response: ByteBuf) {
        if (value >= 128) {
            response.writeShort(value + 32768)
        } else {
            response.writeByte(value)
        }
    }


}