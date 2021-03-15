package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.SKILL_LEVEL
import world.gregs.voidps.network.writeByteInverse
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeIntMiddle

/**
 * Updates the players skill level & experience
 * @param skill The skills id
 * @param level The current players level
 * @param experience The current players experience
 */
fun Client.skillLevel(
    skill: Int,
    level: Int,
    experience: Int
) = send(SKILL_LEVEL, 6) {
    writeByteSubtract(level)
    writeByteInverse(skill)
    writeIntMiddle(experience)
}