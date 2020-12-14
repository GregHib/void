package rs.dusk.core.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import rs.dusk.core.network.connection.Connectable
import rs.dusk.core.network.connection.ConnectionSettings
import rs.dusk.core.network.security.SslServerInitializer

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since March 18, 2020
 */
abstract class NetworkServer(
	
	/**
	 * The event group used for the parent group
	 */
	private val bossGroup : EventLoopGroup = createGroup(
		true
	),
	
	/**
	 * The event group used for the child group
	 */
	private val workerGroup : EventLoopGroup = createGroup(
		false
	)
) : Connectable {
	
	/**
	 * The connection settings to use
	 */
	abstract val settings : ConnectionSettings
	
	private val logger = InlineLogger()
	
	/**
	 * The server bootstrap
	 */
	private var bootstrap = ServerBootstrap().group(bossGroup, workerGroup)
	
	/**
	 * The bootstrap is configured here by preparing the worker groups then binding the relevant options
	 */
	fun configure(initializer : ChannelInitializer<SocketChannel>) = with(bootstrap) {
		channel(NioServerSocketChannel::class.java)
		option(ChannelOption.SO_BACKLOG, 25)
		option(ChannelOption.SO_REUSEADDR, true)
		option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
		childOption(ChannelOption.TCP_NODELAY, true)
		childOption(ChannelOption.SO_KEEPALIVE, true)
		childHandler(initializer)
	}
	
	/**
	 * The server is started by binding the server to the defined port
	 */
	fun bind(sslInitializer : SslServerInitializer? = null) : ChannelFuture = with(bootstrap) {
		val future = bind(settings.port).syncUninterruptibly()
		sslInitializer?.addSslHandler(future.channel())
		logger.info { "Network bound successfully [settings=$settings]" }
		return future
	}
	
	/**
	 * Shuts down the [EventLoopGroup]s used for this server gracefully
	 */
	fun shutdown() {
		bossGroup.shutdownGracefully()
		workerGroup.shutdownGracefully()
	}
	
	companion object {
		fun createGroup(boss : Boolean) : NioEventLoopGroup {
			val serverWorkersCount = if (boss) {
				1
			} else {
				Runtime.getRuntime().availableProcessors() / 2
			}
			return NioEventLoopGroup(serverWorkersCount)
		}
	}
}