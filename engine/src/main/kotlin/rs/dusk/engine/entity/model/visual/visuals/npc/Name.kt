package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Name(var name: String = "") : Visual

const val NAME_MASK = 0x40000

fun NPC.flagName() = visuals.flag(NAME_MASK)

fun NPC.getName() = visuals.getOrPut(NAME_MASK) { Name() }

fun NPC.setName(name: String = "") {
    val n = getName()
    n.name = name
    flagName()
}