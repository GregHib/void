package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

/**
 * @param startX The delta position to start X at
 * @param startY The delta position to start Y at
 * @param startDelay Client ticks until starting the movement
 * @param endX The delta position to move X towards
 * @param endY The delta position to move Y towards
 * @param endDelay Number of client ticks to take moving
 * @param direction The cardinal direction to face during movement
 */
data class ExactMovement(
    var startX: Int = 0,
    var startY: Int = 0,
    var startDelay: Int = 0,
    var endX: Int = 0,
    var endY: Int = 0,
    var endDelay: Int = 0,
    var direction: Int = 0,
) : Visual
