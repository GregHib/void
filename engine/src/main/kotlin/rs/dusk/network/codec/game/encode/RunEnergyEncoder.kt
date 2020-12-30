package rs.dusk.network.codec.game.encode

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.RUN_ENERGY
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class RunEnergyEncoder : Encoder(RUN_ENERGY) {

    /**
     * Sends run energy
     * @param energy The current energy value
     */
    fun encode(
        player: Player,
        energy: Int
    ) = player.send(1) {
        writeByte(energy)
    }
}

fun Player.sendRunEnergy(energy: Int) {
    get<RunEnergyEncoder>().encode(this, energy)
}