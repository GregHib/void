package org.redrune.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import mu.KLogger
import mu.KotlinLogging
import org.redrune.network.channel.RS2ChannelInitializer
import org.redrune.network.codec.message.Message
import org.redrune.network.codec.message.MessageDecoder
import org.redrune.network.codec.message.MessageEncoder
import org.redrune.network.message.encode.LoginResponseMessageEncoder
import org.redrune.tools.PCUtils
import org.redrune.tools.constants.NetworkConstants
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
@ChannelHandler.Sharable
object NetworkBinder {

    /**
     * The map of encoders
     */
    private val decoders = arrayOfNulls<MessageDecoder<*>>(256)

    /**
     * The map of the packete ncoders
     */
    private val encoders = HashMap<KClass<*>, MessageEncoder<*>>()

    /**
     * The logger for this class
     */
    private val logger: KLogger = KotlinLogging.logger {}

    private fun <T : Message> bindEncoder(type: KClass<T>, encoder: MessageEncoder<T>) {
        if (encoders.contains(type)) {
            throw IllegalArgumentException("Cannot have duplicate encoders $type $encoder")
        }
        encoders[type] = encoder
    }

    private inline fun <reified T : Message> bindEncoder(encoder: MessageEncoder<T>) {
        bindEncoder(T::class, encoder)
    }

    fun bindDecoder(decoder: MessageDecoder<*>) {
        if (decoders.contains(decoder)) {
            throw IllegalArgumentException("Cannot have duplicate decoders $decoder")
        }
        decoder.opcodes.forEach { opcode ->
            if (decoders[opcode] != null) {
                throw IllegalArgumentException("Cannot have duplicate decoders $decoder $opcode")
            }
            decoders[opcode] = decoder
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Message> getEncoder(clazz: KClass<T>): MessageEncoder<T>? {
        return encoders[clazz] as? MessageEncoder<T>
    }

    fun getDecoder(opcode: Int): MessageDecoder<*>? {
        return decoders[opcode]
    }

    private fun bindCodec() {
        bindEncoder(LoginResponseMessageEncoder())

    }

    private fun bindSocket(): Boolean {
        val bossGroup = NioEventLoopGroup(PCUtils.PROCESSOR_COUNT)
        val workerGroup = NioEventLoopGroup(PCUtils.PROCESSOR_COUNT)
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

            val future = bootstrap.bind(NetworkConstants.PORT_ID).sync().channel()

            logger.info { "Network bound to port: ${NetworkConstants.PORT_ID}." }

            future.closeFuture().sync()

            return true
        } catch (e: Exception) {
            logger.error("Error initializing network", e)
            return false
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            logger.info { "Network shutdown complete." }
        }
    }

    /**
     * Networking is configured, initialized, and then the port specified [NetworkConstants.PORT_ID] is bound
     */
    fun init(): Boolean {
        return try {
            bindCodec()
            bindSocket()
        } catch (e: Exception) {
            logger.error("Unable to initialize network", e)
            false;
        }
    }

}