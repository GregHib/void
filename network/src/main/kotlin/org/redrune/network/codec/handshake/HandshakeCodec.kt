package org.redrune.network.codec.handshake

import org.redrune.network.codec.Codec
import org.redrune.network.codec.handshake.decoder.LoginServerHandshakeMessageDecoder
import org.redrune.network.codec.handshake.decoder.UpdateServerHandshakeMessageDecoder
import org.redrune.network.codec.handshake.handler.ServiceHandshakeMessageHandler
import org.redrune.network.codec.handshake.handler.ServiceVersionMessageHandler
import org.redrune.network.codec.handshake.message.impl.ServiceHandshakeMessage
import org.redrune.network.codec.handshake.message.impl.ServiceVersionHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object HandshakeCodec : Codec() {

    override fun load() {
        bindDecoder(UpdateServerHandshakeMessageDecoder())
        bindDecoder(LoginServerHandshakeMessageDecoder())

        bindHandler(ServiceVersionHandshakeMessage::class, ServiceVersionMessageHandler())
        bindHandler(ServiceHandshakeMessage::class, ServiceHandshakeMessageHandler())

        report()
    }

}