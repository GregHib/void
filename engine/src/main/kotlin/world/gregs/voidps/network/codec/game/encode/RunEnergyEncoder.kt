package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.RUN_ENERGY
import world.gregs.voidps.utility.get

/**
 * @author GregHib <greg@gregs.world>
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