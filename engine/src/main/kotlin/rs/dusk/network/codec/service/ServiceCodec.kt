package rs.dusk.network.codec.service

import rs.dusk.network.codec.Codec
import rs.dusk.network.codec.service.decode.GameConnectionHandshakeDecoder
import rs.dusk.network.codec.service.decode.UpdateHandshakeDecoder
import rs.dusk.network.codec.service.handle.GameConnectionHandshakeHandler
import rs.dusk.network.codec.service.handle.UpdateHandshakeHandler

class ServiceCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeDecoder())
        registerDecoder(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeDecoder())

        registerHandler(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeHandler())
        registerHandler(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeHandler())
        count = decoders.size
    }
}