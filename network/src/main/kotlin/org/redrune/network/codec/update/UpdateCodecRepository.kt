package org.redrune.network.codec.update

import org.redrune.network.codec.CodecRepository

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