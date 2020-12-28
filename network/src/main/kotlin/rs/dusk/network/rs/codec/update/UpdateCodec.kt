package rs.dusk.network.rs.codec.update

import rs.dusk.core.network.codec.Codec
import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.update.decode.UpdateConnectionMessageDecoder
import rs.dusk.network.rs.codec.update.decode.UpdateDisconnectionMessageDecoder
import rs.dusk.network.rs.codec.update.decode.UpdateLoginStatusMessageDecoder
import rs.dusk.network.rs.codec.update.decode.UpdateRequestMessageDecoder
import rs.dusk.network.rs.codec.update.encode.UpdateRegistryResponseMessageEncoder
import rs.dusk.network.rs.codec.update.encode.UpdateResponseMessageEncoder
import rs.dusk.network.rs.codec.update.encode.UpdateVersionMessageEncoder
import rs.dusk.network.rs.codec.update.handle.UpdateConnectionMessageHandler
import rs.dusk.network.rs.codec.update.handle.UpdateDisconnectionMessageHandler
import rs.dusk.network.rs.codec.update.handle.UpdateLoginStatusHandler
import rs.dusk.network.rs.codec.update.handle.UpdateRequestMessageHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object UpdateCodec : Codec() {

    override fun register() {
        decoders[FileServerOpcodes.CONNECTED] = UpdateConnectionMessageDecoder()
        decoders[FileServerOpcodes.DISCONNECTED] = UpdateDisconnectionMessageDecoder()
        decoders[FileServerOpcodes.STATUS_LOGGED_IN] = UpdateLoginStatusMessageDecoder(true)
        decoders[FileServerOpcodes.STATUS_LOGGED_OUT] = UpdateLoginStatusMessageDecoder(false)
        decoders[FileServerOpcodes.FILE_REQUEST] = UpdateRequestMessageDecoder(false)
        decoders[FileServerOpcodes.PRIORITY_FILE_REQUEST] = UpdateRequestMessageDecoder(true)

        registerHandler(UpdateConnectionMessageHandler())
        registerHandler(UpdateDisconnectionMessageHandler())
        registerHandler(UpdateLoginStatusHandler())
        registerHandler(UpdateRequestMessageHandler())

        registerEncoder(UpdateRegistryResponseMessageEncoder())
        registerEncoder(UpdateResponseMessageEncoder())
        registerEncoder(UpdateVersionMessageEncoder())
    }

}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageDecoder<M : Message>(override var length: Int) : MessageDecoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
*/
abstract class UpdateMessageEncoder<M: Message> : MessageEncoder<M>()

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
abstract class UpdateMessageHandler<M: Message> : MessageHandler<M>()