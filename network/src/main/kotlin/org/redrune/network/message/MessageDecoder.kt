package org.redrune.network.message

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.packet.Packet
import org.redrune.network.packet.PacketReader

/**
 * This class decodes a [Packet] into a [Message]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 3:25 p.m.
 */
abstract class MessageDecoder<T: Message>(val length: Int, vararg val opcodes: Int) {

    /**
     * Handling the decoding of a packet into a [Message] of type [T]
     */
    abstract fun decode(reader: PacketReader, ctx: ChannelHandlerContext): T

}