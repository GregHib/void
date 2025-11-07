package content.entity.player.combat

import content.skill.melee.weapon.attackStyle
import content.skill.melee.weapon.attackType
import content.skill.slayer.isTask
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inject
import kotlin.math.floor

class CombatExperience : Script {

    val definitions: SpellDefinitions by inject()

    init {
        combatAttack { (target, damage, type, _, spell, _) ->
            if (damage <= 0) {
                return@combatAttack
            }
            if (type == "magic" || type == "blaze") {
                val base = definitions.get(spell).experience
                if (get("defensive_cast", false)) {
                    grant(this, target, Skill.Magic, base + damage / 7.5)
                    grant(this, target, Skill.Defence, damage / 10.0)
                } else {
                    grant(this, target, Skill.Magic, base + damage / 5.0)
                }
            } else if (type == "melee" || type == "scorch") {
                when (attackStyle) {
                    "accurate" -> grant(this, target, Skill.Attack, damage / 2.5)
                    "aggressive" -> grant(this, target, Skill.Strength, damage / 2.5)
                    "controlled" -> {
                        grant(this, target, Skill.Attack, damage / 7.5)
                        grant(this, target, Skill.Strength, damage / 7.5)
                        grant(this, target, Skill.Defence, damage / 7.5)
                    }
                    "defensive" -> grant(this, target, Skill.Defence, damage / 2.5)
                }
            } else if (type == "range") {
                if (attackType == "long_range") {
                    grant(this, target, Skill.Ranged, damage / 5.0)
                    grant(this, target, Skill.Defence, damage / 5.0)
                } else {
                    grant(this, target, Skill.Ranged, damage / 2.5)
                }
            }
            if (target is NPC && isTask(target)) {
                grant(this, target, Skill.Slayer, target.def["slayer_xp", 0.0])
            }
            grant(this, target, Skill.Constitution, damage / 7.5)
        }
    }

    fun grant(player: Player, target: Character, skill: Skill, experience: Double) {
        player.exp(skill, experience * calcBonus(target))
    }

    fun calcBonus(target: Character): Double = when (target) {
        is NPC -> {
            val combinedLevels = target.levels.get(Skill.Attack) +
                target.levels.get(Skill.Strength) +
                target.levels.get(Skill.Defence) +
                target.levels.get(Skill.Constitution) / 10
            val combinedAverage = floor(combinedLevels / 4.0)
            val defenceLevels = target["stab_defence", 1] + target["slash_defence", 1] + target["crush_defence", 1]
            val defenceAverage = floor(defenceLevels / 3.0)
            val bonus = defenceAverage + target["strength", 0] + target["attack_bonus", 0]
            1 + 0.025 * floor((combinedAverage * bonus) / 5120)
        }
        is Player -> (1 + 0.025 * floor(target.combatLevel / 20.0)).coerceAtMost(1.125)
        else -> 1.0
    }
}
