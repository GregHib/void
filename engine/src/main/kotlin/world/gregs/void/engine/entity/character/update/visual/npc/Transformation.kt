package world.gregs.void.engine.entity.character.update.visual.npc

import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.update.Visual

/**
 * Changes the characteristics to match NPC with [id]
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Transformation(
    var id: Int = -1
) : Visual

const val TRANSFORM_MASK = 0x20

fun NPC.flagTransform() = visuals.flag(TRANSFORM_MASK)

val NPC.transform: Transformation
    get() = visuals.getOrPut(TRANSFORM_MASK) { Transformation() }