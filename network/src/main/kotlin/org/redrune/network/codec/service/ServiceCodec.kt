package org.redrune.network.codec.service

import org.redrune.core.network.codec.Codec
import org.redrune.network.codec.service.decode.ServiceMessageDecoder
import org.redrune.network.codec.service.encode.ServiceMessageEncoder
import org.redrune.network.codec.service.handle.ServiceMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object ServiceCodec : Codec() {

    override fun register() {
        bindDecoders<ServiceMessageDecoder<*>>()
        bindHandlers<ServiceMessageHandler<*>>()
        bindEncoders<ServiceMessageEncoder<*>>()
        report()
    }
}