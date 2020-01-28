package org.redrune.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.ReferenceCountUtil
import mu.KotlinLogging
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = KotlinLogging.logger {}

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Channel connected: " + ctx.channel().remoteAddress() + ".")
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        try {
            println("Received msg=$msg")
            val session = ctx.channel().attr(Session.SESSION_KEY).get()
            ctx.channel().attr(Session.SESSION_KEY).get().printPipeline()
            if (session != null && msg is Message) {
                session.messageReceived(msg)
            }
        } finally {
            println("retaining msg = $msg")

            if (msg is ByteBuf) {
                val bldr = StringBuilder()
                ByteBufUtil.appendPrettyHexDump(bldr, msg)
                println("$bldr\n")
            }
            ReferenceCountUtil.retain(msg)
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
        System.out.println("nw we flsug!@#!@#!@#")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable) {
        if (e.toString().contains("An existing connection was forcibly closed by the remote host")) {
            return
        }
        e.printStackTrace()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.channel().close()
        logger.info("inactive channel closed")
    }


}
