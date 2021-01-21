package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.LOGOUT_LOBBY

/**
 * @author GregHib <greg@gregs.world>
 * @since July 27, 2020
 */
class LogoutLobbyEncoder : Encoder(LOGOUT_LOBBY) {

    fun encode(player: Player) = player.send(0) {}
}