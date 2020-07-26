package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Sends run energy
 * @param energy The current energy value
 */
data class RunEnergyMessage(val energy: Int) : Message