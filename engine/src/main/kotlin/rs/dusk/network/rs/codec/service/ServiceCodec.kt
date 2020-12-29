package rs.dusk.network.rs.codec.service

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.service.decode.GameConnectionHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.decode.UpdateHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.handle.GameConnectionHandshakeMessageHandler
import rs.dusk.network.rs.codec.service.handle.UpdateHandshakeMessageHandler

class ServiceCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeMessageDecoder())
        registerDecoder(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeMessageDecoder())

        registerHandler(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeMessageHandler())
        registerHandler(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeMessageHandler())
        count = decoders.size + encoders.size
    }
}