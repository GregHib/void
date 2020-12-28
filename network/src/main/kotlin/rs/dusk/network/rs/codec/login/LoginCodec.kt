package rs.dusk.network.rs.codec.login

import rs.dusk.core.network.codec.Codec
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.login.decode.GameLoginMessageDecoder
import rs.dusk.network.rs.codec.login.decode.LobbyLoginMessageDecoder
import rs.dusk.network.rs.codec.login.encode.GameLoginConnectionResponseMessageEncoder
import rs.dusk.network.rs.codec.login.encode.GameLoginDetailsMessageEncoder
import rs.dusk.network.rs.codec.login.encode.LobbyConfigurationMessageEncoder
import rs.dusk.network.rs.codec.login.encode.LobbyLoginConnectionResponseMessageEncoder
import rs.dusk.network.rs.codec.login.handle.LobbyLoginMessageHandler
import rs.dusk.network.rs.codec.service.ServiceOpcodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object LoginCodec : Codec() {

    override fun register() {
        decoders[GameOpcodes.GAME_LOGIN] = GameLoginMessageDecoder()
        decoders[ServiceOpcodes.LOBBY_LOGIN] = LobbyLoginMessageDecoder()

        registerHandler(LobbyLoginMessageHandler())

        registerEncoder(GameLoginConnectionResponseMessageEncoder())
        registerEncoder(GameLoginDetailsMessageEncoder())
        registerEncoder(LobbyConfigurationMessageEncoder())
        registerEncoder(LobbyLoginConnectionResponseMessageEncoder())
    }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageDecoder<M : Message>(override var length: Int) : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageHandler<M : Message> : MessageHandler<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class LoginMessageEncoder<M : Message> : MessageEncoder<M>()