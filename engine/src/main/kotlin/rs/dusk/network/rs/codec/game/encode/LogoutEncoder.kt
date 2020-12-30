package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.LOGOUT

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class LogoutEncoder : Encoder(LOGOUT) {

    fun encode(player: Player) = player.send(0) {}
}