package world.gregs.void.network.codec.login

import world.gregs.void.network.codec.Codec
import world.gregs.void.network.codec.game.GameOpcodes
import world.gregs.void.network.codec.login.decode.GameLoginDecoder
import world.gregs.void.network.codec.login.decode.LobbyLoginDecoder
import world.gregs.void.network.codec.login.handle.LobbyLoginHandler
import world.gregs.void.network.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginHandler())
        count = decoders.size
    }
}