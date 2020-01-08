package org.redrune.network.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.packet.PacketConstants
import org.redrune.network.packet.struct.PacketHeader
import org.redrune.network.packet.struct.IncomingPacket

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class RS2PacketDecoder(session: NetworkSession) : ReplayingDecoder<RS2PacketDecoder.PacketStage>() {
    /**
     * The opcode of the current packed being decoded
     */
    private var opcode = 0

    /**
     * The length of the current packet being decoded
     */
    private var length = 0

    init {
        state(PacketStage.VERSION)
        session.channel.attr(NetworkConstants.SESSION_KEY).setIfAbsent(session)
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (state()) {
            PacketStage.VERSION -> {
                opcode = `in`.readUnsignedByte().toInt()
                checkpoint(PacketStage.PAYLOAD_LENGTH)
            }
            PacketStage.PAYLOAD_LENGTH -> {
                length = PacketConstants.PACKET_LENGTHS[opcode]
                when (length) {
                    -1 -> {
                        length = `in`.readUnsignedByte().toInt()
                    }
                    -2 -> {
                        length = `in`.readUnsignedShort()
                    }
                    -3 -> {
                        length = `in`.readInt()
                    }
                }
                checkpoint(PacketStage.PAYLOAD)
            }
            PacketStage.PAYLOAD -> {
                try {
                    val payload = ByteArray(length)
                    `in`.readBytes(payload, 0, length)
                    `in`.markReaderIndex()
                    out.add(IncomingPacket(opcode, PacketHeader.STANDARD, Unpooled.copiedBuffer(payload)))
                } catch (e: Exception) {
                    println("Packet[$opcode, $length]")
                    ctx.fireExceptionCaught(e)
                }
                checkpoint(PacketStage.VERSION)
            }
        }
    }

    enum class PacketStage {
        VERSION, PAYLOAD_LENGTH, PAYLOAD
    }
}
