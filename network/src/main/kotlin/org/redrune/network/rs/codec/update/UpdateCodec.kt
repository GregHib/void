package org.redrune.network.rs.codec.update

import org.redrune.core.network.codec.Codec
import org.redrune.core.network.codec.message.MessageDecoder
import org.redrune.core.network.codec.message.MessageEncoder
import org.redrune.core.network.codec.message.MessageHandler
import org.redrune.core.network.model.message.Message
import org.redrune.network.rs.codec.update.UpdateMessageDecoder
import org.redrune.network.rs.codec.update.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.UpdateMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object UpdateCodec : Codec() {

    override fun register() {
        bindDecoders<UpdateMessageDecoder<*>>()
        bindHandlers<UpdateMessageHandler<*>>()
        bindEncoders<UpdateMessageEncoder<*>>()
        report()
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