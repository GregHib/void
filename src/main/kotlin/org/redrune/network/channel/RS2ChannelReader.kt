package org.redrune.network.channel

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.redrune.network.packet.Packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
class RS2ChannelReader : SimpleChannelInboundHandler<Packet>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Packet) {

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        //println("ctx = [$ctx], e = [$cause]");
        // ctx.channel().close()
    }

}