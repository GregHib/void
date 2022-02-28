package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * Changes the characteristics to match NPC with [id]
 */
data class Transformation(
    var id: Int = -1
) : Visual

const val TRANSFORM_MASK = 0x2

fun NPC.flagTransform() = visuals.flag(TRANSFORM_MASK)

val NPC.transform: Transformation
    get() = visuals.transform