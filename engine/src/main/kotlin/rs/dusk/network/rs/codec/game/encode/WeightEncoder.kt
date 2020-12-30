package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.Encoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.PLAYER_WEIGHT

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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