package rs.dusk.network.rs.codec.service

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.service.decode.GameConnectionHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.decode.UpdateHandshakeMessageDecoder
import rs.dusk.network.rs.codec.service.handle.GameConnectionHandshakeMessageHandler
import rs.dusk.network.rs.codec.service.handle.UpdateHandshakeMessageHandler

object ServiceCodec : Codec() {

    override fun register() {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeMessageDecoder())
        registerDecoder(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeMessageDecoder())

        registerHandler(GameConnectionHandshakeMessageHandler())
        registerHandler(UpdateHandshakeMessageHandler())
    }
}