package world.gregs.voidps.network.codec.login

import world.gregs.voidps.network.codec.Codec
import world.gregs.voidps.network.codec.game.GameOpcodes
import world.gregs.voidps.network.codec.login.decode.GameLoginDecoder
import world.gregs.voidps.network.codec.login.decode.LobbyLoginDecoder
import world.gregs.voidps.network.codec.login.handle.LobbyLoginHandler
import world.gregs.voidps.network.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginHandler())
        count = decoders.size
    }
}