package org.redrune.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import mu.KLogger
import mu.KotlinLogging
import org.redrune.tools.PCUtils.Companion.PROCESSOR_COUNT
import org.redrune.tools.constants.NetworkConstants
import org.redrune.tools.constants.NetworkConstants.Companion.PORT_ID
import java.net.InetSocketAddress

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
object NetworkBinder {

    /**
     * The logger for this class
     */
    private val logger: KLogger = KotlinLogging.logger {}

    @Throws(InterruptedException::class)
    fun bind(): Boolean {
        val bossGroup = NioEventLoopGroup(PROCESSOR_COUNT)
        val workerGroup = NioEventLoopGroup(PROCESSOR_COUNT)
        try {

            val bootstrap = ServerBootstrap()

            bootstrap.group(bossGroup, workerGroup)
            bootstrap.channel(NioServerSocketChannel::class.java)
            bootstrap.option(SO_BACKLOG, 25)
            bootstrap.option(SO_REUSEADDR, true)
            bootstrap.option(ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            bootstrap.option(CONNECT_TIMEOUT_MILLIS, NetworkConstants.TIMEOUT_RATE)
            bootstrap.childOption(TCP_NODELAY, true)
            bootstrap.childOption(SO_KEEPALIVE, true)
            bootstrap.childOption(WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
            bootstrap.childOption(WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
            bootstrap.childHandler(NetworkChannelInitializer())
            bootstrap.bind(InetSocketAddress(PORT_ID)).sync()

            logger.info { "Network bound to port: $PORT_ID." }

            return true
        } catch (e: Exception) {
            logger.error("Error initializing network", e)
            return false
        }
    }

}