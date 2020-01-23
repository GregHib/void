package org.redrune.network.codec.service

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020
 */
class ServiceDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) return

        val opcode = buf.readUnsignedByte().toInt()
        println("opcode=$opcode")
        val service: Service = Service.values().first { it -> it.opcode == opcode }
        println("service=$service")
        out.add(ServiceRequest(service))
    }
}