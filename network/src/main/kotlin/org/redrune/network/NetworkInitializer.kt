package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.redrune.network.codec.service.ServiceCodec
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.update.UpdateCodec
import org.redrune.network.model.message.InboundMessageDecoder
import org.redrune.network.model.message.OutboundSimpleMessageEncoder
import org.redrune.network.model.packet.SimplePacketDecoder
import org.redrune.network.session.Session
import org.redrune.tools.constants.NetworkConstants
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
@ChannelHandler.Sharable
class NetworkInitializer : ChannelInitializer<SocketChannel>() {

    private val logger = InlineLogger()

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        with(pipeline) {
            addLast(LoggingHandler(LogLevel.INFO))

            // todo design this better for changing codec
            addLast("packet.decoder", SimplePacketDecoder(ServiceCodec))
            addLast("message.decoder", InboundMessageDecoder(ServiceCodec))
            addLast("network.handler", NetworkHandler(ServiceCodec))
            addLast("message.encode", OutboundSimpleMessageEncoder(ServiceCodec))
        }
    }

    fun init() : NetworkInitializer {
        ServiceCodec.load()
        UpdateCodec.load()
        LoginCodec.load()
        return this
    }

    @Throws(InterruptedException::class)
    fun bind(): Boolean {
        return try {
            val bootstrap = NetworkBootstrap()
            bootstrap.childHandler(NetworkInitializer())
            bootstrap.bind(InetSocketAddress(NetworkConstants.PORT_ID)).sync()
            logger.info { "Network bound to port: ${NetworkConstants.PORT_ID}" }
            true
        } catch (e: Exception) {
            logger.error(e) { "Error initializing network" }
            false
        }
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
fun ChannelPipeline.getPipelineContents(): MutableList<String>? {
    val list = mutableListOf<String>()
    val names = names()
    names.forEach { list.add(it) }
    return names
}

/**
 * Returns the contents of the buffer in a readable format (hexadecimal)
 */
fun ByteBuf.getHexContents(): String {
    val dump = StringBuilder()
    ByteBufUtil.appendPrettyHexDump(dump, this)
    return dump.toString()
}