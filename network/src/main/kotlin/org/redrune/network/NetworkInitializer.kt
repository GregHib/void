package org.redrune.network

import com.github.michaelbull.logging.InlineLogger
import com.google.common.base.Stopwatch
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.redrune.core.network.model.message.codec.impl.RS2MessageDecoder
import org.redrune.core.network.model.message.codec.impl.RawMessageEncoder
import org.redrune.core.network.model.packet.codec.impl.SimplePacketDecoder
import org.redrune.core.network.model.session.Session
import org.redrune.core.network.model.session.setSession
import org.redrune.network.codec.game.GameCodec
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.service.ServiceCodec
import org.redrune.network.codec.update.UpdateCodec
import org.redrune.utility.constants.NetworkConstants
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
@ChannelHandler.Sharable
class NetworkInitializer : ChannelInitializer<SocketChannel>() {

    private val logger = InlineLogger()

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.apply {
            //            addLast(LoggingHandler(LogLevel.INFO))
            addLast("packet.decoder", SimplePacketDecoder(ServiceCodec))
            addLast(
                "message.decoder",
                RS2MessageDecoder(ServiceCodec)
            )
            addLast(
                "message.handler",
                NetworkChannelHandler(ServiceCodec)
            )
            addLast(
                "message.encoder",
                RawMessageEncoder(ServiceCodec)
            )
        }
        ch.setSession(Session(ch))
    }

    // TODO: do this upon construction of codec
    fun init(): NetworkInitializer {
        val stopwatch = Stopwatch.createStarted()
        ServiceCodec.register()
        UpdateCodec.register()
        LoginCodec.register()
        GameCodec.register()
        logger.info { "Took ${stopwatch.elapsed(TimeUnit.MILLISECONDS)}ms to prepare all codecs" }
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
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
private class NetworkBootstrap(
    bossGroup: EventLoopGroup = createGroup(true),
    workerGroup: EventLoopGroup = createGroup(false)
) : ServerBootstrap() {

    init {
        group(bossGroup, workerGroup)
        channel(NioServerSocketChannel::class.java)
        option(ChannelOption.SO_BACKLOG, 25)
        option(ChannelOption.SO_REUSEADDR, true)
        option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        option(ChannelOption.CONNECT_TIMEOUT_MILLIS, NetworkConstants.TIMEOUT_RATE)
        childOption(ChannelOption.TCP_NODELAY, true)
        childOption(ChannelOption.SO_KEEPALIVE, true)
        childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(8 * 1024, 32 * 1024))
    }

    companion object {
        fun createGroup(boss: Boolean): NioEventLoopGroup {
            val serverWorkersCount = if (boss) {
                1
            } else {
                val processors = Runtime.getRuntime().availableProcessors()
                if (processors >= 6) processors - if (processors >= 12) 7 else 5 else 1
            }
            return NioEventLoopGroup(serverWorkersCount)
        }
    }
}