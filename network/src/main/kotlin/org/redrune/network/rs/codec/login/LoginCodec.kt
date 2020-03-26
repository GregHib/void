package org.redrune.network.rs.codec.login

import org.redrune.core.network.codec.Codec
import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.model.message.Message
import org.redrune.network.rs.codec.login.LoginMessageDecoder
import org.redrune.network.rs.codec.login.LoginMessageEncoder
import org.redrune.network.rs.codec.login.LoginMessageHandler

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

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageDecoder<M : Message> : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageHandler<M : Message> : MessageHandler<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageEncoder<M : Message> : MessageEncoder<M>()