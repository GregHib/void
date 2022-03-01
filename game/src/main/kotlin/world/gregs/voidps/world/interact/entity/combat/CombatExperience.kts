import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.attackType
import kotlin.math.floor

val definitions: SpellDefinitions by inject()

on<CombatAttack>({ damage > 0 }) { player: Player ->
    if (type == "magic" || type == "blaze") {
        val base = definitions.get(spell).experience
        if (player.getVar("defensive_cast", false)) {
            grant(player, target, Skill.Magic, base + damage / 7.5)
            grant(player, target, Skill.Defence, damage / 10.0)
        } else {
            grant(player, target, Skill.Magic, base + damage / 5.0)
        }
    } else if (type == "range") {
        if (player.attackType == "long_range") {
            grant(player, target, Skill.Ranged, damage / 5.0)
            grant(player, target, Skill.Defence, damage / 5.0)
        } else {
            grant(player, target, Skill.Ranged, damage / 2.5)
        }
    } else if (type == "melee") {
        when (player.attackStyle) {
            "accurate" -> grant(player, target, Skill.Attack, damage / 2.5)
            "aggressive" -> grant(player, target, Skill.Strength, damage / 2.5)
            "controlled" -> {
                grant(player, target, Skill.Attack, damage / 7.5)
                grant(player, target, Skill.Strength, damage / 7.5)
                grant(player, target, Skill.Defence, damage / 7.5)
            }
            "defensive" -> grant(player, target, Skill.Defence, damage / 2.5)
        }
    }
    if (target is NPC && player.isTask(target)) {
        grant(player, target, Skill.Slayer, target.def["slayer_xp", 0.0])
    }
    grant(player, target, Skill.Constitution, damage / 7.5)
}

fun grant(player: Player, target: Character, skill: Skill, experience: Double) {
    player.exp(skill, experience * calcBonus(target))
}

fun calcBonus(target: Character): Double {
    return when (target) {
        is NPC -> {
            val combinedLevels = target.levels.get(Skill.Attack) +
                    target.levels.get(Skill.Strength) +
                    target.levels.get(Skill.Defence) +
                    target.levels.get(Skill.Constitution) / 10
            val combinedAverage = floor(combinedLevels / 4.0)
            val defenceLevels = target["stab_def", 1] + target["slash_def", 1] + target["crush_def", 1]
            val defenceAverage = floor(defenceLevels / 3.0)
            val bonus = defenceAverage + target["str_bonus", 0] + target["att_bonus", 0]
            1 + 0.025 * floor((combinedAverage * bonus) / 5120)
        }
        is Player -> (1 + 0.025 * floor(target.combatLevel / 20.0)).coerceAtMost(1.125)
        else -> 1.0
    }
}