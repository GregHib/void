package world.gregs.void.network.codec.service

import world.gregs.void.network.codec.Codec
import world.gregs.void.network.codec.service.decode.GameConnectionHandshakeDecoder
import world.gregs.void.network.codec.service.decode.UpdateHandshakeDecoder
import world.gregs.void.network.codec.service.handle.GameConnectionHandshakeHandler
import world.gregs.void.network.codec.service.handle.UpdateHandshakeHandler

class ServiceCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeDecoder())
        registerDecoder(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeDecoder())

        registerHandler(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeHandler())
        registerHandler(ServiceOpcodes.FILE_SERVICE, UpdateHandshakeHandler())
        count = decoders.size
    }
}