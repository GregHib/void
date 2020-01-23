package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.redrune.network.codec.service.ServiceDecoder
import org.redrune.network.codec.service.ServiceEncoder
import org.redrune.network.session.ServiceSession
import org.redrune.network.session.Session
import org.redrune.tools.constants.NetworkConstants.Companion.PORT_ID
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
class NetworkInitializer : ChannelInitializer<SocketChannel>() {

    /**
     * The logger for this class
     */
    private val logger = LoggerFactory.getLogger(NetworkInitializer::class.java)

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()

        pipeline.apply {
            addLast("service.decoder", ServiceDecoder())
            addLast("service.encoder", ServiceEncoder())
        }
        pipeline.apply {
            addLast("reader", NetworkReader())
        }

        ch.attr(Session.SESSION_KEY).set(ServiceSession(pipeline.channel()))
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