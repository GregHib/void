package org.redrune.network.codec

import org.redrune.network.codec.handshake.UpdateMessageDecoder
import org.redrune.network.codec.handshake.UpdateMessageEncoder
import org.redrune.network.codec.handshake.UpdateMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 4:02 p.m.
 */
object UpdateCodecRepository : CodecRepository() {

    override fun initialize() {
        bindDecoders<UpdateMessageDecoder<*>>()
        bindEncoders<UpdateMessageEncoder<*>>()
        bindHandlers<UpdateMessageHandler<*>>()
    }

}