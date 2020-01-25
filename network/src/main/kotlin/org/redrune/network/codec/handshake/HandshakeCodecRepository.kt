package org.redrune.network.codec.handshake

import org.redrune.network.codec.CodecRepository
import org.redrune.network.codec.handshake.decode.HandshakeMessageDecoder
import org.redrune.network.codec.handshake.encode.HandshakeResponseEncoder
import org.redrune.network.codec.handshake.handler.HandshakeMessageHandler
import org.redrune.network.codec.handshake.message.HandshakeMessage
import org.redrune.network.codec.handshake.message.HandshakeResponse
import org.redrune.network.codec.update.UpdateCodecRepository
import org.redrune.network.codec.update.UpdateMessageDecoder
import org.redrune.network.codec.update.UpdateMessageEncoder
import org.redrune.network.codec.update.UpdateMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020
 */
object HandshakeCodecRepository : CodecRepository() {
    override fun initialize() {
        bindDecoder(HandshakeMessageDecoder())
        bindHandler(HandshakeMessage::class, HandshakeMessageHandler())
        bindEncoder(HandshakeResponse::class, HandshakeResponseEncoder())
    }

}