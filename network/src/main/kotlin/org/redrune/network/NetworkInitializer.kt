package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import mu.KotlinLogging
import org.redrune.network.codec.handshake.HandshakeCodecRepository
import org.redrune.network.codec.handshake.HandshakeSession
import org.redrune.network.codec.update.UpdateCodecRepository
import org.redrune.network.message.SimpleMessageDecoder
import org.redrune.network.message.SimpleMessageEncoder
import org.redrune.network.packet.SimplePacketDecoder
import org.redrune.network.packet.SimplePacketEncoder
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
        val session = HandshakeSession(ch, HandshakeCodecRepository)
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
        ch.attr(Session.SESSION_KEY).set(session)
    }

    @Throws(InterruptedException::class)
    fun bind(): Boolean {
        return try {
            HandshakeCodecRepository.initialize()
            UpdateCodecRepository.initialize()
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