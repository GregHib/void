package rs.dusk.network.rs.codec.service

import rs.dusk.core.network.codec.Codec
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.service.decode.GameConnectionHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.decode.UpdateHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.handle.GameConnectionHandshakeMessageHandler
import rs.dusk.network.rs.codec.service.handle.UpdateHandshakeMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object ServiceCodec : Codec() {

    override fun register() {
        decoders[ServiceOpcodes.GAME_CONNECTION] = GameConnectionHandshakeMessageDecoder()
        decoders[ServiceOpcodes.FILE_SERVICE] = UpdateHandshakeMessageDecoder()

        registerHandler(GameConnectionHandshakeMessageHandler())
        registerHandler(UpdateHandshakeMessageHandler())
    }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageHandler<M : Message> : MessageHandler<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageEncoder<M: Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class ServiceMessageDecoder<M : Message>(override var length: Int) : MessageDecoder<M>()