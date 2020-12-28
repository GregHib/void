package rs.dusk.network.rs.codec.login

import rs.dusk.core.network.codec.Codec
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.login.decode.GameLoginMessageDecoder
import rs.dusk.network.rs.codec.login.decode.LobbyLoginMessageDecoder
import rs.dusk.network.rs.codec.login.encode.GameLoginConnectionResponseMessageEncoder
import rs.dusk.network.rs.codec.login.encode.GameLoginDetailsMessageEncoder
import rs.dusk.network.rs.codec.login.encode.LobbyConfigurationMessageEncoder
import rs.dusk.network.rs.codec.login.encode.LobbyLoginConnectionResponseMessageEncoder
import rs.dusk.network.rs.codec.login.handle.LobbyLoginMessageHandler
import rs.dusk.network.rs.codec.service.ServiceOpcodes

object LoginCodec : Codec() {

    override fun register() {
        registerDecoder(GameOpcodes.GAME_LOGIN, GameLoginMessageDecoder())
        registerDecoder(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginMessageDecoder())

        registerHandler(ServiceOpcodes.LOBBY_LOGIN, LobbyLoginMessageHandler())

        registerEncoder(GameLoginConnectionResponseMessageEncoder())
        registerEncoder(GameLoginDetailsMessageEncoder())
        registerEncoder(LobbyConfigurationMessageEncoder())
        registerEncoder(LobbyLoginConnectionResponseMessageEncoder())
    }
}