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

fun NPC.flagTransform() = visuals.flag(0x20)

fun NPC.getTransform() = visuals.getOrPut(Transform::class) { Transform() }

fun NPC.setTransform(id: Int = -1) {
    val transform = getTransform()
    transform.id = id
    flagTransform()
}
