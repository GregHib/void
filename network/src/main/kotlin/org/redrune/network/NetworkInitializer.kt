package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import mu.KotlinLogging
import org.redrune.network.codec.handshake.HandshakeDecoder
import org.redrune.network.codec.handshake.HandshakeSession
import org.redrune.tools.constants.NetworkConstants.Companion.PORT_ID
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
class NetworkInitializer : ChannelInitializer<SocketChannel>() {

    private val logger = KotlinLogging.logger {}

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(ReadTimeoutHandler(5), HandshakeDecoder(), NetworkHandler())
        ch.attr(Session.SESSION_KEY).set(HandshakeSession(ch))
    }

    @Throws(InterruptedException::class)
    fun bind(): Boolean {
        return try {
            val bootstrap = NetworkBootstrap()
            bootstrap.childHandler(NetworkInitializer())
            bootstrap.bind(InetSocketAddress(PORT_ID)).sync()
            logger.info("Network bound to port: $PORT_ID")
            true
        } catch (e: Exception) {
            logger.error("Error initializing network", e)
            false
        }
    }

}