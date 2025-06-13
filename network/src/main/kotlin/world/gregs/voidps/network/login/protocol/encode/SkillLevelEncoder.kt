package world.gregs.voidps.network.login.protocol.encode

import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.SKILL_LEVEL
import world.gregs.voidps.network.login.protocol.writeByteInverse
import world.gregs.voidps.network.login.protocol.writeByteSubtract
import world.gregs.voidps.network.login.protocol.writeIntMiddle

/**
 * Updates the players' skill level & experience
 * @param skill The skills id
 * @param level The current players level
 * @param experience The current players experience
 */
fun Client.skillLevel(
    skill: Int,
    level: Int,
    experience: Int,
) = send(SKILL_LEVEL) {
    writeByteSubtract(level)
    writeByteInverse(skill)
    writeIntMiddle(experience)
}
