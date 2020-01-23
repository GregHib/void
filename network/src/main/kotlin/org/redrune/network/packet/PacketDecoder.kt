package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.redrune.network.session.GameSession
import org.redrune.tools.constants.PacketConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class PacketDecoder(private val session: GameSession) : ReplayingDecoder<PacketDecoder.PacketStage>() {
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
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (state()) {
            PacketStage.VERSION -> {
                val encryptedOpcode = `in`.readUnsignedByte().toInt()
                opcode = encryptedOpcode - (session.isaacPair?.decodingRandom?.nextInt()!! and 0xFF)
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
                    out.add(PacketBuilder(opcode = opcode, buffer = Unpooled.copiedBuffer(payload)).build())
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
