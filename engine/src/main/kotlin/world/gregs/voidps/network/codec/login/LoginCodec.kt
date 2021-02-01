package world.gregs.voidps.network.codec.login

import world.gregs.voidps.network.codec.Codec
import world.gregs.voidps.network.codec.game.GameOpcodes
import world.gregs.voidps.network.codec.login.decode.GameLoginDecoder
import world.gregs.voidps.network.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(ServiceOpcodes.GAME_LOGIN, GameLoginDecoder())
        registerDecoder(ServiceOpcodes.GAME_RECONNECT, GameLoginDecoder())
        count = decoders.size
    }
}