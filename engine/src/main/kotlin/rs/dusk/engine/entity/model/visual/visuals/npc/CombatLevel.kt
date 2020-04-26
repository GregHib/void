package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class CombatLevel(var level: Int = 1) : Visual

fun NPC.getCombatLevel() = visuals.getOrPut(CombatLevel::class) { CombatLevel() }

fun NPC.flagCombatLevel() = visuals.flag(0x80000)

fun NPC.setCombatLevel(level: Int) {
    val combat = getCombatLevel()
    combat.level = level
    flagCombatLevel()
}