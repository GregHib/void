package org.redrune.network

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import mu.KotlinLogging
import org.redrune.network.codec.CodecRepository
import org.redrune.network.message.Message
import org.redrune.network.message.MessageHandler
import org.redrune.tools.constants.NetworkConstants
import java.net.InetSocketAddress

/**
 * This class represents the network session from the client (player) to the server
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
abstract class Session(
    /**
     * The channel for the connection
     */
    var channel: Channel,

    /**
     * The codec for this session
     */
    val codec: CodecRepository
) {

    private val logger = KotlinLogging.logger {}

    /**
     * When a message is received, this function is invoked
     */
    open fun messageReceived(msg: Message) {
        val handler = codec.handler(msg::class) as? MessageHandler<Message>
            ?: run {
                logger.warn("No handler for message: $msg, codec=$codec")
                return
            }
        handler.handle(this, msg)
    }

    /**
     * Sends a message to the channel by [Channel.writeAndFlush]
     */
    fun send(msg: Any) {
        channel.writeAndFlush(msg)
    }

    fun getHost(): String = (channel.remoteAddress() as? InetSocketAddress)?.address?.hostAddress
        ?: NetworkConstants.LOCALHOST

    fun printPipeline() {
        val pipelineHandlers: StringBuilder = StringBuilder("")
        channel.pipeline().forEach { pipelineHandlers.append("${it.value.javaClass.simpleName}, ") }
        println(pipelineHandlers)
    }

    companion object {
        /**
         * The attribute that maps to the session of a channel
         */
        val SESSION_KEY: AttributeKey<Session> = AttributeKey.valueOf<Session>("session.key")

        /**
         * The attribute that maps to the encryption key
         */
        val ENCRYPTION_KEY = AttributeKey.valueOf<Int>("encryption.key")!!
    }

}