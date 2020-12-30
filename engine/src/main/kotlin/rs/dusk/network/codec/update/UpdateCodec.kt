package rs.dusk.network.codec.update

import rs.dusk.network.codec.Codec
import rs.dusk.network.codec.login.encode.LoginResponseEncoder
import rs.dusk.network.codec.update.decode.UpdateConnectionDecoder
import rs.dusk.network.codec.update.decode.UpdateDisconnectionDecoder
import rs.dusk.network.codec.update.decode.UpdateLoginStatusDecoder
import rs.dusk.network.codec.update.decode.UpdateRequestDecoder
import rs.dusk.network.codec.update.handle.UpdateConnectionHandler
import rs.dusk.network.codec.update.handle.UpdateDisconnectionHandler
import rs.dusk.network.codec.update.handle.UpdateLoginStatusHandler
import rs.dusk.network.codec.update.handle.UpdateRequestHandler

class UpdateCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(FileServerOpcodes.CONNECTED, UpdateConnectionDecoder())
        registerDecoder(FileServerOpcodes.DISCONNECTED, UpdateDisconnectionDecoder())
        registerDecoder(FileServerOpcodes.STATUS_LOGGED_IN, UpdateLoginStatusDecoder(true))
        registerDecoder(FileServerOpcodes.STATUS_LOGGED_OUT, UpdateLoginStatusDecoder(false))
        registerDecoder(FileServerOpcodes.FILE_REQUEST, UpdateRequestDecoder(false))
        registerDecoder(FileServerOpcodes.PRIORITY_FILE_REQUEST, UpdateRequestDecoder(true))

        val responseEncoder = LoginResponseEncoder()
        registerHandler(FileServerOpcodes.CONNECTED, UpdateConnectionHandler(responseEncoder))
        registerHandler(FileServerOpcodes.DISCONNECTED, UpdateDisconnectionHandler())
        registerHandler(FileServerOpcodes.STATUS_LOGGED_IN, UpdateLoginStatusHandler(responseEncoder))
        registerHandler(FileServerOpcodes.STATUS_LOGGED_OUT, UpdateLoginStatusHandler(responseEncoder))
        registerHandler(FileServerOpcodes.PRIORITY_FILE_REQUEST, UpdateRequestHandler())

        count = decoders.size
    }

}