package rs.dusk.network.rs.codec.update

import rs.dusk.core.network.codec.Codec
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

object UpdateCodec : Codec() {

    override fun register() {
        registerDecoder(FileServerOpcodes.CONNECTED, UpdateConnectionMessageDecoder())
        registerDecoder(FileServerOpcodes.DISCONNECTED, UpdateDisconnectionMessageDecoder())
        registerDecoder(FileServerOpcodes.STATUS_LOGGED_IN, UpdateLoginStatusMessageDecoder(true))
        registerDecoder(FileServerOpcodes.STATUS_LOGGED_OUT, UpdateLoginStatusMessageDecoder(false))
        registerDecoder(FileServerOpcodes.FILE_REQUEST, UpdateRequestMessageDecoder(false))
        registerDecoder(FileServerOpcodes.PRIORITY_FILE_REQUEST, UpdateRequestMessageDecoder(true))

        registerHandler(FileServerOpcodes.CONNECTED, UpdateConnectionMessageHandler())
        registerHandler(FileServerOpcodes.DISCONNECTED, UpdateDisconnectionMessageHandler())
        registerHandler(FileServerOpcodes.STATUS_LOGGED_IN, UpdateLoginStatusHandler())
        registerHandler(FileServerOpcodes.STATUS_LOGGED_OUT, UpdateLoginStatusHandler())
        registerHandler(FileServerOpcodes.PRIORITY_FILE_REQUEST, UpdateRequestMessageHandler())

        registerEncoder(UpdateRegistryResponseMessageEncoder())
        registerEncoder(UpdateResponseMessageEncoder())
        registerEncoder(UpdateVersionMessageEncoder())
    }

}