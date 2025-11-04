package content.entity.player.combat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.summoningCombatLevel
import kotlin.math.max

class CombatLevel : Script {

    init {
        playerSpawn {
            combatLevel = calculateCombatLevel(levels)
            summoningCombatLevel = calculateCombatLevel(levels, true)
        }

        for (skill in Skill.entries.filter { it.ordinal <= 6 || it.ordinal == 23 }.toTypedArray()) {
            maxLevelChanged(skill, ::recalculate)
        }
    }

    fun recalculate(player: Player, skill: Skill, from: Int, to: Int) {
        player.combatLevel = calculateCombatLevel(player.levels)
        player.summoningCombatLevel = calculateCombatLevel(player.levels, true)
    }

    fun calculateCombatLevel(levels: Levels, summoning: Boolean = false): Int {
        val melee = levels.getMax(Skill.Attack) + levels.getMax(Skill.Strength)
        val ranged = (levels.getMax(Skill.Ranged) * 3) / 2
        val mage = (levels.getMax(Skill.Magic) * 3) / 2
        val highest = max(melee, max(ranged, mage)) * 13
        var def = levels.getMax(Skill.Defence) + (levels.getMax(Skill.Constitution) / 10) + (levels.getMax(Skill.Prayer) / 2)
        if (World.members && summoning) {
            def += levels.getMax(Skill.Summoning) / 2
        }
        return ((highest / 10) + def) / 4
    }
}
