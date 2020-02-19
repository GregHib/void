package org.redrune.network.codec.file

import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.game.encode.GameMessageEncoder
import org.redrune.network.codec.game.handle.GameMessageHandler
import org.redrune.network.codec.Codec
import org.redrune.network.codec.file.decode.FileServerMessageDecoder
import org.redrune.network.codec.file.encode.FileServerMessageEncoder
import org.redrune.network.codec.file.handle.FileServerMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object FileCodec : Codec() {

    override fun register() {
        bindDecoders<FileServerMessageDecoder<*>>()
        bindHandlers<FileServerMessageHandler<*>>()
        bindEncoders<FileServerMessageEncoder<*>>()
        report()
    }

}