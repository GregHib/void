package org.redrune.network.rs.codec.update

import org.redrune.core.network.codec.Codec
import org.redrune.network.rs.codec.update.decode.UpdateMessageDecoder
import org.redrune.network.rs.codec.update.encode.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.handle.UpdateMessageHandler

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