package rs.dusk.network.rs.codec.update

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
class UpdateCodec : Codec() {

    private val logger = InlineLogger()

    override fun register() {
        bindDecoders<UpdateMessageDecoder<*>>()
        bindHandlers<UpdateMessageHandler<*>>()
        bindEncoders<UpdateMessageEncoder<*>>()
//        report(logger)
    }

}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageDecoder<M : Message> : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
*/
abstract class UpdateMessageEncoder<M: Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageHandler<M: Message> : MessageHandler<M>()