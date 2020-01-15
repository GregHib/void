package org.redrune.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.redrune.network.channel.RS2ChannelInitializer
import org.redrune.util.PCUtils

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
object NetworkBinder {

    private val bossGroup = NioEventLoopGroup(PCUtils.PROCESSOR_COUNT)
    private val workerGroup = NioEventLoopGroup(PCUtils.PROCESSOR_COUNT)

    /**
     * Networking is configured, initialized, and then the port specified [NetworkConstants.PORT_ID] is bound
     */
    fun init(): Boolean {
        try {

            val bootstrap = ServerBootstrap()

            // builds the bootstrap
            bootstrap.group(bossGroup, workerGroup)
            bootstrap.channel(NioServerSocketChannel::class.java)
            bootstrap.option(ChannelOption.SO_BACKLOG, 25)
            bootstrap.option(ChannelOption.SO_REUSEADDR, true)
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, NetworkConstants.TIMEOUT_RATE)
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
            bootstrap.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
            bootstrap.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
            bootstrap.childHandler(RS2ChannelInitializer())

            val future: ChannelFuture = bootstrap.bind(NetworkConstants.PORT_ID).sync()

            println("Network bound to port: ${NetworkConstants.PORT_ID}.")

            future.channel().closeFuture().sync()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            println("Network shutdown complete.")
        }
    }

}