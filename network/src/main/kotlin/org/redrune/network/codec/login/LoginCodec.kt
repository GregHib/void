package org.redrune.network.codec.login

import org.redrune.network.codec.Codec
import org.redrune.network.codec.login.decoder.LobbyLoginMessageDecoder
import org.redrune.network.codec.login.encoder.LoginHandshakeResponseMessageEncoder
import org.redrune.network.codec.login.message.LoginHandshakeResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object LoginCodec : Codec() {
    override fun load() {
        bindDecoder(LobbyLoginMessageDecoder())

        bindEncoder(LoginHandshakeResponseMessage::class, LoginHandshakeResponseMessageEncoder())

        report()
    }
}