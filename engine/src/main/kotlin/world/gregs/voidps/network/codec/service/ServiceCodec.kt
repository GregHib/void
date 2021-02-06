package world.gregs.voidps.network.codec.service

import world.gregs.voidps.network.codec.Codec
import world.gregs.voidps.network.codec.service.decode.GameConnectionHandshakeDecoder
import world.gregs.voidps.network.codec.service.decode.UpdateHandshakeDecoder
import world.gregs.voidps.network.codec.service.handle.GameConnectionHandshakeHandler

class ServiceCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeDecoder())
        registerHandler(ServiceOpcodes.GAME_CONNECTION, GameConnectionHandshakeHandler())
        count = decoders.size
    }
}