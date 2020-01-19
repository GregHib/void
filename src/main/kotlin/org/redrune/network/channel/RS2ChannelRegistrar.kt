package org.redrune.network.channel

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession

@ChannelHandler.Sharable
class   RS2ChannelRegistrar : ChannelInboundHandlerAdapter() {

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        val session: NetworkSession? = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
        session?.state = NetworkSession.SessionState.REGISTERED
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        val session: NetworkSession? = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
        session?.state = NetworkSession.SessionState.DEREGISTERED
    }
}
