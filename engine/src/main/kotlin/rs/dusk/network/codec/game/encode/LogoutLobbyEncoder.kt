package rs.dusk.network.codec.game.encode

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.LOGOUT_LOBBY

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class LogoutLobbyEncoder : Encoder(LOGOUT_LOBBY) {

    fun encode(player: Player) = player.send(0) {}
}