package org.redrune.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class NetworkBootstrap(
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
        childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
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