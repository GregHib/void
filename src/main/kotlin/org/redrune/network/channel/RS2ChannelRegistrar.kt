package org.redrune.network.channel

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession

@ChannelHandler.Sharable
class   RS2ChannelRegistrar : ChannelInboundHandlerAdapter() {

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        println("RS2ChannelRegistrar.channelRegistered")
        val session: NetworkSession? = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
        if (session == null) {
            println("Channel disconnected with no session")
            return
        }

    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        println("RS2ChannelRegistrar.channelUnregistered")
    }
}
