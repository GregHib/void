package rs.dusk.network.rs.codec.login

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.login.decode.GameLoginDecoder
import rs.dusk.network.rs.codec.login.decode.LobbyLoginDecoder
import rs.dusk.network.rs.codec.login.handle.LobbyLoginHandler
import rs.dusk.network.rs.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginHandler())
        count = decoders.size
    }
}