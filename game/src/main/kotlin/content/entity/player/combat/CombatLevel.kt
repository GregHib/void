package content.entity.player.combat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
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
            combatLevel = calculateCombatLevel(levels, summoning = World.members)
            summoningCombatLevel = calculateCombatLevel(levels, summoning = true)
        }

        for (skill in Skill.entries.filter { it.ordinal <= 6 || it.ordinal == 23 }.toTypedArray()) {
            maxLevelChanged(skill, ::recalculate)
        }
    }

    fun recalculate(player: Player, skill: Skill, from: Int, to: Int) {
        val previous = player.summoningCombatLevel
        val level = calculateCombatLevel(player.levels, summoning = World.members)
        player.combatLevel = level
        player.summoningCombatLevel = calculateCombatLevel(player.levels, summoning = true)
        if (player["skip_level_up", false]) {
            return
        }
        if (level > previous) {
            when (level) {
                90, 100, 110, 120, 130 -> {
                    // 90 - https://youtu.be/Jjn4-f9eSMM?t=47
                    // 100 - https://youtu.be/sk72rWo2VPI?t=219
                    // 130 - https://youtu.be/bHnKZatcA2s?t=10
                    player.message("<red>Well done! You've reached the Combat level $level milestone!")
                }
                126 -> {
                    // f2p - https://youtu.be/31_wDe_HJsU?t=64
                    // mem - https://youtu.be/uKJpOSzeTpg?t=53
                    player.message("<red>Congratulations! Your Combat level is now 126 - the highest possible Combat level on free worlds!")
                }
                138 -> {
                    // https://youtu.be/o6Ga2FGG1Dc?t=329
                    player.message("<red>Congratulations! Your Combat level is now 138! You've achieved the highest Combat level possible!")
                }
                else -> {
                    // 30 - https://youtu.be/b4vTaGjIk8Q?t=11
                    // 80 - https://youtu.be/GKKIH2hca0E?t=68
                    // 112 - https://youtu.be/3GG4KkmPUrU?t=70
                    // 125 - https://youtu.be/lFY_jQn3pfE?t=54
                    // 128 - https://youtu.be/tBB914-pLEw?t=28
                    player.message("Congratulations! You've just reached Combat level $level!")
                }
            }
        }
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
