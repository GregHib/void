package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.hasSlayerTask
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.activity.skill.slayer.undead
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.antifire
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.superAntifire
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

    fun slayer(player: Player, target: Character, type: String, damage: Boolean): Double {
        if (type == "melee" && target is NPC && target.undead) {
            when (player.equipped(EquipSlot.Amulet).id) {
                "salve_amulet_e" -> return 1.2
                "salve_amulet" -> return 7.0 / 6.0
            }
        }
        if (!player.hasSlayerTask || !player.isTask(target)) {
            return 1.0
        }
        val helm = player.equipped(EquipSlot.Hat).id
        if (type == "melee" && (helm.startsWith("black_mask") || helm.startsWith("slayer_helmet"))) {
            return 7.0 / 6.0
        }
        if (type == "range" && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
            return 1.15
        }
        if (damage && type == "magic" && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
            return 1.15
        }
        return 1.0
    }

    fun dragonfire(player: Player, target: Character): Double {
        val metal = target is NPC && (target.id.contains("bronze") || target.id.contains("iron") || target.id.contains("steel"))
        var multiplier = 1.0

        val shield = player.equipped(EquipSlot.Shield).id
        if (shield == "anti_dragon_shield" || shield.startsWith("dragonfire_shield")) {
            multiplier -= if (metal) 0.6 else 0.8
            player.message("Your shield absorbs most of the dragon's fiery breath!", ChatType.Filter)
        }

        if (player.antifire || player.superAntifire) {
            multiplier -= if (player.superAntifire) 1.0 else 0.5
        }

        if (multiplier > 0.0) {
            val black = target is NPC && target.id.contains("black")
            if (!metal && !black && random.nextDouble() <= 0.1) {
                multiplier -= 0.1
                player.message("You manage to resist some of the dragon fire!", ChatType.Filter)
            } else {
                player.message("You're horribly burnt by the dragon fire!", ChatType.Filter)
            }
        }
        return multiplier.coerceAtLeast(0.0)
    }
}