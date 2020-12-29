package rs.dusk.network.rs.codec.login

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.login.decode.GameLoginMessageDecoder
import rs.dusk.network.rs.codec.login.decode.LobbyLoginMessageDecoder
import rs.dusk.network.rs.codec.login.handle.LobbyLoginMessageHandler
import rs.dusk.network.rs.codec.service.ServiceOpcodes

class LoginCodec : Codec() {

    override fun load(args: Array<out Any?>) {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginMessageDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginMessageDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginMessageHandler())
        count = decoders.size
    }
}