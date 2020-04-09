package org.redrune.network.rs.codec.game

import com.github.michaelbull.logging.InlineLogger
import org.redrune.core.network.codec.Codec
import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object GameCodec : Codec() {

    override fun register() {
        bindDecoders<GameMessageDecoder<*>>()
        bindHandlers<GameMessageHandler<*>>()
        bindEncoders<GameMessageEncoder<*>>()
    }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageEncoder<M : Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageDecoder<M : Message> : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class GameMessageHandler<M : Message> : MessageHandler<M>()