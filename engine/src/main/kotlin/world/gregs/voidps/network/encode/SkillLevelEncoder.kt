package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByteInverse
import world.gregs.voidps.buffer.write.writeByteSubtract
import world.gregs.voidps.buffer.write.writeIntMiddle
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.SKILL_LEVEL

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