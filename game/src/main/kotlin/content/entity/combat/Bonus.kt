package content.entity.combat

import content.entity.combat.hit.Hit
import content.skill.melee.weapon.attackStyle
import content.skill.slayer.isTask
import content.skill.slayer.slayerTask
import content.skill.slayer.undead
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object Bonus {
    fun stance(character: Character, skill: Skill): Int = 8 + when {
        character is NPC -> 1
        (skill == Skill.Attack || skill == Skill.Ranged) && character.attackStyle == "accurate" -> 3
        (skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && character.attackStyle == "controlled" -> 1
        skill == Skill.Defence && (character.attackStyle == "defensive" || character.attackStyle == "long_range") -> 3
        skill == Skill.Strength && character.attackStyle == "aggressive" -> 3
        skill == Skill.Magic -> 1
        else -> 0
    }

    fun slayerModifier(source: Character, target: Character, type: String, value: Int, damage: Boolean): Int {
        if (source !is Player) {
            return value
        }
        if (Hit.meleeType(type) && target is NPC && target.undead) {
            when (source.equipped(EquipSlot.Amulet).id) {
                "salve_amulet_e" -> return (value * 1.2).toInt()
                "salve_amulet" -> return (value * (7.0 / 6.0)).toInt()
            }
        }
        if (source.slayerTask == "nothing" || !source.isTask(target)) {
            return value
        }
        val helm = source.equipped(EquipSlot.Hat).id
        if (Hit.meleeType(type) && (helm.startsWith("black_mask") || helm.startsWith("slayer_helmet"))) {
            return (value * (7.0 / 6.0)).toInt()
        } else if (type == "range" && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
            return (value * 1.15).toInt()
        } else if (damage && type == "magic" && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
            return (value * 1.15).toInt()
        }
        return value
    }
}
