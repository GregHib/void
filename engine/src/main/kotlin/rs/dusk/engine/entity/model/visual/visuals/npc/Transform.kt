package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.visual.Visual

/**
 * Changes the characteristics to match NPC with [id]
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Transform(
    var id: Int = -1
) : Visual

const val TRANSFORM_MASK = 0x20

fun NPC.flagTransform() = visuals.flag(TRANSFORM_MASK)

fun NPC.getTransform() = visuals.getOrPut(TRANSFORM_MASK) { Transform() }

fun NPC.setTransform(id: Int = -1) {
    val transform = getTransform()
    transform.id = id
    flagTransform()
}
