package org.redrune.network.rs.codec.service

import com.github.michaelbull.logging.InlineLogger
import org.redrune.core.network.codec.Codec
import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.model.message.Message
import org.redrune.network.rs.codec.service.ServiceMessageDecoder
import org.redrune.network.rs.codec.service.ServiceMessageEncoder
import org.redrune.network.rs.codec.service.ServiceMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object ServiceCodec : Codec() {

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