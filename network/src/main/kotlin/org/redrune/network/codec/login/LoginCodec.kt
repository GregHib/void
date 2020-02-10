package org.redrune.network.codec.login

import org.redrune.network.codec.Codec
import org.redrune.network.codec.login.decoder.LobbyLoginMessageDecoder
import org.redrune.network.codec.login.encoder.LobbyConstructionMessageEncoder
import org.redrune.network.codec.login.encoder.LoginResponseMessageEncoder
import org.redrune.network.codec.login.encoder.LoginServiceResponseMessageEncoder
import org.redrune.network.codec.login.handler.LobbyLoginMessageHandler
import org.redrune.network.codec.login.message.LobbyConstructionMessage
import org.redrune.network.codec.login.message.LobbyLoginMessage
import org.redrune.network.codec.login.message.LoginResponseMessage
import org.redrune.network.codec.login.message.LoginServiceResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object LoginCodec : Codec() {
    override fun load() {
        bindDecoder(LobbyLoginMessageDecoder())

        bindHandler(LobbyLoginMessage::class, LobbyLoginMessageHandler())

        bindEncoder(LobbyConstructionMessage::class, LobbyConstructionMessageEncoder())
        bindEncoder(LoginResponseMessage::class, LoginResponseMessageEncoder())
        bindEncoder(LoginServiceResponseMessage::class, LoginServiceResponseMessageEncoder())

        report()
    }
}