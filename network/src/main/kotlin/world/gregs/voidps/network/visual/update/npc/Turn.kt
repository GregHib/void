package world.gregs.voidps.network.visual.update.npc

import world.gregs.voidps.network.Visual

/**
 * Turn an NPC to face [directionX], [directionY]
 */
data class Turn(
    var x: Int = 0,
    var y: Int = 0,
    var directionX: Int = 0,
    var directionY: Int = 0,
    var direction: Int = 0
) : Visual