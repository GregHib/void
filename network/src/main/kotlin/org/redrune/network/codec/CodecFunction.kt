package org.redrune.network.codec

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageHandler
import org.redrune.network.model.packet.Packet
import org.redrune.network.model.packet.PacketBuilder
import org.redrune.network.model.packet.PacketReader
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
interface CodecFunction {

    /**
     * Decodes from the packet reader and returns a message
     */
    fun decode(reader: PacketReader): Message

    /**
     * Handles a message and returns if it was successful
     */
    fun <T : Message> handle(clazz: KClass<*>, ctx: ChannelHandlerContext, msg: T): MessageHandler<T>

    /**
     * Encodes a message and returns if it was successful
     */
    fun <T : Message> encode(
        clazz: KClass<*>,
        msg: T,
        out: PacketBuilder
    ): Packet

    /**
     * Gets the size of a packet
     */
    fun getLength(opcode: Int): Int
}