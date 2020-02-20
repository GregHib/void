package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.*
import org.redrune.network.session.Session
import org.redrune.tools.func.NetworkFunc

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
@ChannelHandler.Sharable
object NetworkHandler : ChannelInboundHandlerAdapter() {

    private val logger = InlineLogger()

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Channel inactive: " + ctx.channel().remoteAddress() + "." }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

}

/**
 * Gets the object in the [Session.SESSION_KEY] attribute
 * @receiver Channel
 * @return Session
 */
fun Channel.getSession(): Session {
    return attr(Session.SESSION_KEY).get()
}

/**
 * Sets the [Session.SESSION_KEY] attribute
 */
fun Channel.setSession(session: Session) {
    attr(Session.SESSION_KEY).set(session)
}

/**
 * Returns the contents of the pipeline in order from head to tail as a [List] of type [String]
 * @receiver Channel
 * @return String
 */
fun ChannelPipeline.getPipelineContents(): String {
    val list = mutableMapOf<String, String>()
    forEach { list[it.key] = it.value.javaClass.simpleName }
    return list.toString()
}

/**
 * Returns the contents of the buffer in a readable format (hexadecimal)
 */
// TODO convert to one lined hex dump
fun ByteBuf.getHexContents(): String {
    return ByteBufUtil.hexDump(toByteArraySafe())
}

fun ByteBuf.toByteArraySafe(): ByteArray {
    if (this.hasArray()) {
        return this.array()
    }

    val bytes = ByteArray(this.readableBytes())
    this.getBytes(this.readerIndex(), bytes)

    return bytes
}

fun ChannelPipeline.replace(name: String, handler: ChannelHandler): ChannelHandler? {
    return replace(name, name, handler)
}