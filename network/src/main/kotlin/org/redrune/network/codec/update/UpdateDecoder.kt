package org.redrune.network.codec.update

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.redrune.network.codec.handshake.message.VersionMessage
import org.redrune.network.codec.update.message.FileRequest
import org.redrune.network.codec.update.message.FileRequestType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 6:45 p.m.
 */
class UpdateDecoder : ReplayingDecoder<UpdateDecoder.State>(State.READ_VERSION) {

    enum class State { READ_VERSION, READ_REQUEST }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        println("State=${state()}")
        when (state()) {
            State.READ_VERSION -> {
                val version = buf.readInt()
                println("version=$version")
                out.add(VersionMessage(version))
                state(State.READ_REQUEST)
            }
            State.READ_REQUEST -> {
                val opcode: Int = buf.readUnsignedByte().toInt()
                val requestType = FileRequestType.values().find { it.opcode == opcode }
                println("request=")
                when (requestType) {
                    FileRequestType.FILE_REQUEST, FileRequestType.PRIORITY_FILE_REQUEST -> {
                        val type = buf.readUnsignedByte().toInt()
                        val file = buf.readUnsignedShort()
                        out.add(FileRequest(type, file, requestType == FileRequestType.PRIORITY_FILE_REQUEST))
                    }
                    FileRequestType.CLIENT_LOGGED_IN -> {
                        TODO()
                    }
                    FileRequestType.CLIENT_LOGGED_OUT -> {
                        TODO()
                    }
                    FileRequestType.ENCRYPTION -> {
                        TODO()
                    }
                    FileRequestType.CONNECTION_INITIATED -> {
                        TODO()
                    }
                    FileRequestType.CONNECTION_TERMINATED -> {
                        TODO()
                    }
                }
            }
        }
    }
}