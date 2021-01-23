package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class CombatLevel(var level: Int = 1) : Visual

const val COMBAT_LEVEL_MASK = 0x80000

fun NPC.flagCombatLevel() = visuals.flag(COMBAT_LEVEL_MASK)

fun NPC.getCombatLevel() = visuals.getOrPut(COMBAT_LEVEL_MASK) { CombatLevel() }

var NPC.combatLevel: Int
    get() = getCombatLevel().level
    set(value) {
        getCombatLevel().level = value
        flagCombatLevel()
    }