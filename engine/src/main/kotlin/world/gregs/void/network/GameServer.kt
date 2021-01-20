package world.gregs.void.network

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.bytes.ByteArrayEncoder
import io.netty.util.concurrent.GlobalEventExecutor
import org.koin.dsl.module
import world.gregs.void.engine.client.Sessions
import world.gregs.void.network.codec.game.GameCodec
import world.gregs.void.network.codec.login.LoginCodec
import world.gregs.void.network.codec.service.ServiceCodec
import world.gregs.void.network.codec.setCodec
import world.gregs.void.network.codec.update.UpdateCodec
import world.gregs.void.network.connection.ChannelAdapter
import world.gregs.void.network.connection.DisconnectQueue
import world.gregs.void.network.packet.PacketDecoder
import world.gregs.void.utility.get

class GameServer(
    val port: Int,
    private val bossGroup: EventLoopGroup = createGroup(true),
    private val workerGroup: EventLoopGroup = createGroup(false)
) {

    private val logger = InlineLogger()
    private val channels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    private var bootstrap = ServerBootstrap().group(bossGroup, workerGroup)
    var running = false

    fun run() {
        val service: ServiceCodec = get()
        val sessions: Sessions = get()
        val disconnections: DisconnectQueue = get()

        bootstrap.apply {
            channel(NioServerSocketChannel::class.java)
            option(ChannelOption.SO_BACKLOG, 25)
            option(ChannelOption.SO_REUSEADDR, true)
            option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            childOption(ChannelOption.TCP_NODELAY, true)
            childOption(ChannelOption.SO_KEEPALIVE, true)
            val encoder = ByteArrayEncoder()
            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val pipe = ch.pipeline()
                    pipe.addLast("packet.decoder", PacketDecoder())
                    pipe.addLast("packet.encoder", encoder)
                    pipe.addLast("channel.listener", ChannelAdapter(channels, sessions, disconnections))
                    pipe.channel().setCodec(service)
                }
            })
            bind(port).syncUninterruptibly()
        }
        logger.info { "Network bound successfully [port=$port]" }
        running = true
    }

    fun shutdown() {
        running = false
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }

    companion object {
        private fun createGroup(boss: Boolean) = NioEventLoopGroup(if (boss) 1 else Runtime.getRuntime().availableProcessors() / 2)
    }
}

val networkCodecs = module {
    single(createdAtStart = true) { GameCodec().apply { run() } }
    single(createdAtStart = true) { LoginCodec().apply { run() } }
    single(createdAtStart = true) { ServiceCodec().apply { run() } }
    single(createdAtStart = true) { UpdateCodec().apply { run() } }
}