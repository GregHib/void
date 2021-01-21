package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.PLAYER_WEIGHT

/**
 * @author GregHib <greg@gregs.world>
 * @since September 13, 2020
 */
class WeightEncoder : Encoder(PLAYER_WEIGHT) {

    /**
     * Updates player weight for equipment screen
     */
    fun encode(
        player: Player,
        weight: Int
    ) = player.send(2) {
        writeShort(weight)
    }
}