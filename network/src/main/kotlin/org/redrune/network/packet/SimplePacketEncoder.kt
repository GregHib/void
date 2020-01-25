package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 6:10 p.m.
 */
class SimplePacketEncoder : PacketEncoder() {

    override fun getContents(packet: Packet, ctx: ChannelHandlerContext): ByteBuf {
        println("yo were getting contex")
        return packet.buffer
    }
}