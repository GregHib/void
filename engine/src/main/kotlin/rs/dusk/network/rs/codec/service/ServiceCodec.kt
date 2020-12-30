package rs.dusk.network.rs.codec.service

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.service.decode.GameConnectionHandshakeDecoder
import rs.dusk.network.rs.codec.service.decode.UpdateHandshakeDecoder
import rs.dusk.network.rs.codec.service.handle.GameConnectionHandshakeHandler
import rs.dusk.network.rs.codec.service.handle.UpdateHandshakeHandler

class ServiceCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeDecoder())
        registerDecoder(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeDecoder())

        registerHandler(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeHandler())
        registerHandler(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeHandler())
        count = decoders.size
    }
}