package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import mu.KotlinLogging
import org.redrune.network.codec.handshake.HandshakeDecoder
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
        val pipeline = ch.pipeline()
        pipeline.addFirst(LoggingHandler(LogLevel.INFO))
        pipeline.addLast("handshake.decoder", HandshakeDecoder())
        pipeline.addLast("handler", NetworkHandler())
        ch.attr(Session.SESSION_KEY).set(Session(ch))
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

/**

//        session.printPipeline()
ch.pipeline().apply {
addLast(
//                LoggingHandler(LogLevel.INFO),
//                ReadTimeoutHandler(5),
// client -> packet -> message
SimplePacketDecoder(),
// message -> handler
SimpleMessageDecoder(),
// handler
NetworkHandler(),
// packet -> out
SimplePacketEncoder(),
// message -> packet
SimpleMessageEncoder()
)
}
 */