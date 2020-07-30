package rs.dusk.engine.entity.character.update.visual.npc

import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCEvent
import rs.dusk.engine.entity.character.update.Visual

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

fun NPCEvent.transform(id: Int) {
    npc.transform = id
}

var NPC.transform: Int
    get() = getTransform().id
    set(value) {
        getTransform().id = value
        flagTransform()
    }