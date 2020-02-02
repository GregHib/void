package org.redrune.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
abstract class NetworkHandler<I> : SimpleChannelInboundHandler<I>() {

    /*
    private val decoders = arrayOfNulls<MessageDecoder<*>>(256)
    private val encoders = HashMap<KClass<*>, MessageEncoder<*>>()
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    /**
     * Sends a message to through the pipeline
     */
    fun send(ctx: ChannelHandlerContext, msg: Any) {
        ctx.pipeline().writeAndFlush(msg)
    }

}