package org.redrune.network

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import mu.KotlinLogging
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

    private val updateCodec = UpdateCodecRepository

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(
//                LoggingHandler(LogLevel.INFO),
//                ReadTimeoutHandler(5),
                // client -> packet -> message
                SimplePacketDecoder(updateCodec),
                // message -> handler
                SimpleMessageDecoder(updateCodec),
                // handler
                NetworkHandler(),
                // packet -> out
                SimplePacketEncoder(),
                // message -> packet
                SimpleMessageEncoder(updateCodec)
            )
        }
        val handshakeSession = HandshakeSession(ch, updateCodec)
        handshakeSession.printPipeline()
        ch.attr(Session.SESSION_KEY).set(handshakeSession)
    }

    @Throws(InterruptedException::class)
    fun bind(): Boolean {
        return try {
            updateCodec.initialize()
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