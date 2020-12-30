package rs.dusk.network.codec.login

import rs.dusk.network.codec.Codec
import rs.dusk.network.codec.game.GameOpcodes
import rs.dusk.network.codec.login.decode.GameLoginDecoder
import rs.dusk.network.codec.login.decode.LobbyLoginDecoder
import rs.dusk.network.codec.login.handle.LobbyLoginHandler
import rs.dusk.network.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginHandler())
        count = decoders.size
    }
}