package org.redrune.network.rs.codec.game

import org.redrune.core.network.codec.Codec
import org.redrune.network.rs.codec.game.decode.GameMessageDecoder
import org.redrune.network.rs.codec.game.encode.GameMessageEncoder
import org.redrune.network.rs.codec.game.handle.GameMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object GameCodec : Codec() {

    override fun register() {
        bindDecoders<GameMessageDecoder<*>>()
        bindHandlers<GameMessageHandler<*>>()
        bindEncoders<GameMessageEncoder<*>>()
        report()
    }
}