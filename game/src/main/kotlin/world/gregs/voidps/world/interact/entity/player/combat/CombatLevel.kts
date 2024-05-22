package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.maxLevelChange
import world.gregs.voidps.engine.entity.character.player.summoningCombatLevel
import world.gregs.voidps.engine.entity.playerSpawn
import kotlin.math.max

playerSpawn { player ->
    player.combatLevel = calculateCombatLevel(player.levels)
    player.summoningCombatLevel = calculateCombatLevel(player.levels, true)
}

val combatSkills = Skill.entries.filter { it.ordinal <= 6 || it.ordinal == 23 }.toTypedArray()

maxLevelChange(skills = combatSkills) { player ->
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