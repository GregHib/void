package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.LOGOUT

/**
 * @author GregHib <greg@gregs.world>
 * @since July 27, 2020
 */
class LogoutEncoder : Encoder(LOGOUT) {

    fun encode(player: Player) = player.send(0) {}
}