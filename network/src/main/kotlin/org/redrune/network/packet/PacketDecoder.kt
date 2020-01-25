package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import mu.KotlinLogging
import org.redrune.network.packet.struct.PacketHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
abstract class PacketDecoder :
    ReplayingDecoder<PacketDecoder.PacketStage>(PacketStage.READ_OPCODE) {
    private val logger = KotlinLogging.logger {}

    /**
     * The opcode of the current packed being decoded
     */
    private var opcode = 0

    /**
     * The length of the current packet being decoded
     */
    private var length = 0

    /**
     * If the length is variable
     */
    private var header = PacketHeader.FIXED

    enum class PacketStage {
        READ_OPCODE, READ_LENGTH, READ_PAYLOAD
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        when (state()) {
            PacketStage.READ_OPCODE -> {
                if (!buf.isReadable) {
                    return
                }
                opcode = readOpcode(buf)
                checkpoint(PacketStage.READ_LENGTH)
            }
            PacketStage.READ_LENGTH -> {
                if (!buf.isReadable) {
                    logger.warn { "Unable to read length of buffer [opcode=$opcode]" }
                    return
                }
                length = getLength(ctx, opcode)
                    ?: throw IllegalArgumentException("Invalid opcode, unable to identify packet size [opcode=$opcode]")
                header = getHeader(length, buf)
                checkpoint(PacketStage.READ_PAYLOAD)
            }
            PacketStage.READ_PAYLOAD -> {
                if (buf.readableBytes() < length) {
                    logger.warn { "Unable to read payload of buffer [opcode=$opcode, length=$length, readableBytes=${buf.readableBytes()}]" }
                    return
                }
                val payload = buf.readBytes(length)
                out.add(
                    Packet(
                        opcode = opcode,
                        header = this.header,
                        buffer = Unpooled.copiedBuffer(payload)
                    )
                )
                checkpoint(PacketStage.READ_OPCODE)
            }
        }
    }

    /**
     * Returns the packet opcode
     * @param buf The buffer to read from
     * @return The packets opcode
     */
    open fun readOpcode(buf: ByteBuf): Int {
        return buf.readUnsignedByte().toInt()
    }

    open fun getHeader(length: Int, buf: ByteBuf): PacketHeader {
        return when (length) {
            -1 -> {
                buf.readUnsignedByte().toInt()
                PacketHeader.VARIABLE_BYTE
            }
            -2 -> {
                buf.readUnsignedShort()
                PacketHeader.VARIABLE_SHORT
            }
            else -> {
                throw  IllegalStateException("Unable to find header for packet $opcode")
            }
        }
    }

    /**
     * Returns the expected size of a packet
     * @param ctx Channel context
     * @param opcode The packet who's size to get
     * @return The expected size (if any)
     */
    abstract fun getLength(ctx: ChannelHandlerContext, opcode: Int): Int?
}
