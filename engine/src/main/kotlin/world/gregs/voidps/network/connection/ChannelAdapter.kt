package world.gregs.voidps.network.connection

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import world.gregs.voidps.engine.client.Sessions

class ChannelAdapter(
    private val collection: MutableCollection<Channel>,
    private val sessions: Sessions,
    private val disconnections: DisconnectQueue
) : ChannelInboundHandlerAdapter() {
    private val logger = InlineLogger()

    override fun channelRegistered(ctx: ChannelHandlerContext) {
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        val session = ctx.channel()
        val player = sessions.get(session) ?: return
        disconnections.add(player)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.debug { "A connection has been registered and added to the set of live connections!" }
        collection.add(ctx.channel())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "A connection has been de-registered and removed from total list!" }
        collection.remove(ctx.channel())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

}