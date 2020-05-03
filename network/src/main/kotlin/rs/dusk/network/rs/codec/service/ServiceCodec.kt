package rs.dusk.network.rs.codec.service

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.core.network.codec.Codec
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class ServiceCodec : Codec() {

    private val logger = InlineLogger()

    override fun register() {
        bindDecoders<ServiceMessageDecoder<*>>()
        bindHandlers<ServiceMessageHandler<*>>()
        bindEncoders<ServiceMessageEncoder<*>>()
//        report(logger)
    }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageHandler<M : Message> : MessageHandler<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageEncoder<M: Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageDecoder<M : Message> : MessageDecoder<M>()