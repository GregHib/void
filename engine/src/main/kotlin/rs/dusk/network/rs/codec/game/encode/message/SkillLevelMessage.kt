package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message


/**
 * Updates the players skill level & experience
 * @param skill The skills id
 * @param level The current players level
 * @param experience The current players experience
 */
data class SkillLevelMessage(val skill: Int, val level: Int, val experience: Int) : Message