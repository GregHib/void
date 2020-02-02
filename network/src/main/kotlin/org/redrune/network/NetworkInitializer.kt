package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.redrune.network.codec.handshake.HandshakeDecoder
import org.redrune.network.codec.handshake.HandshakeHandler
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
            addLast(LoggingHandler(LogLevel.INFO), HandshakeDecoder(), HandshakeHandler())
        }
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

