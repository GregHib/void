package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * Updates player weight for equipment screen
 */
data class WeightMessage(val weight: Int) : Message