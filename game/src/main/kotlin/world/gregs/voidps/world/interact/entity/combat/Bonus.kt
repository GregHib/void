package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.prayer.getBaseDrain
import world.gregs.voidps.world.interact.entity.player.combat.prayer.getDrain
import world.gregs.voidps.world.interact.entity.player.combat.prayer.getLeech
import kotlin.math.floor

object Bonus {
    fun prayer(character: Character, skill: Skill, accuracy: Boolean): Double {
        return when (character) {
            is NPC -> 1.0 - ((character.getBaseDrain(skill) + character.getDrain(skill)) / 100.0)
            is Player -> {
                val style = if (skill == Skill.Ranged) if (accuracy) "_attack" else "_strength" else ""
                var bonus = character["base_${skill.name.lowercase()}${style}_bonus", 1.0]
                if (character.equipped(EquipSlot.Amulet).id == "amulet_of_zealots") {
                    bonus = floor(1.0 + (bonus - 1.0) * 2)
                }
                bonus += if (character["turmoil", false]) {
                    character["turmoil_${skill.name.lowercase()}_bonus", 0].toDouble() / 100.0
                } else {
                    character.getLeech(skill) * 100.0 / character.levels.getMax(skill) / 100.0
                }
                bonus -= character.getBaseDrain(skill) + character.getDrain(skill) / 100.0
                bonus
            }
            else -> 1.0
        }
    }

    fun stance(character: Character, skill: Skill): Int {
        return 8 + when {
            character is NPC -> 1
            (skill == Skill.Attack || skill == Skill.Ranged) && character.attackStyle == "accurate" -> 3
            (skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && character.attackStyle == "controlled" -> 1
            skill == Skill.Defence && (character.attackStyle == "defensive" || character.attackStyle == "long_range") -> 3
            skill == Skill.Strength && character.attackStyle == "aggressive" -> 3
            skill == Skill.Magic -> 1
            else -> 0
        }
    }
}