package org.redrune.network.rs.codec.login

import org.redrune.core.network.codec.Codec
import org.redrune.network.rs.codec.login.decode.LoginMessageDecoder
import org.redrune.network.rs.codec.login.encode.LoginMessageEncoder
import org.redrune.network.rs.codec.login.handle.LoginMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object LoginCodec : Codec() {
    override fun register() {
        bindDecoders<LoginMessageDecoder<*>>()
        bindHandlers<LoginMessageHandler<*>>()
        bindEncoders<LoginMessageEncoder<*>>()
        report()
    }
}