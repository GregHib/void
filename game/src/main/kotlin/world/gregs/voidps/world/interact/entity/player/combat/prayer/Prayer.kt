package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import kotlin.math.floor

object Prayer {

    fun hasAnyActive(player: Player): Boolean {
        return (player.variables as PlayerVariables).temp.any { (key, value) -> key.startsWith("prayer_") && value == true }
    }

    fun setTurmoilTarget(source: Character, target: Character) {
        if (source.praying("turmoil") && source.get<Int>("turmoil_target") != if (target is NPC) -target.index else target.index) {
            source["turmoil_attack_bonus"] = (target.levels.get(Skill.Attack).coerceAtMost(99) * 0.15).toInt()
            source["turmoil_strength_bonus"] = (target.levels.get(Skill.Strength).coerceAtMost(99) * 0.1).toInt()
            source["turmoil_defence_bonus"] = (target.levels.get(Skill.Defence).coerceAtMost(99) * 0.15).toInt()
            source["turmoil"] = true
            source["turmoil_target"] = if (target is NPC) -target.index else target.index
        } else if (!source.praying("turmoil") && source.contains("turmoil")) {
            source.clear("turmoil")
            source.clear("turmoil_target")
            source.clear("turmoil_attack_bonus")
            source.clear("turmoil_strength_bonus")
            source.clear("turmoil_defence_bonus")
        }
    }

    fun effectiveLevelModifier(character: Character, skill: Skill, accuracy: Boolean, level: Int): Int {
        val multiplier = when (character) {
            is NPC -> 1.0 - ((character.getBaseDrain(skill) + character.getDrain(skill)) / 100.0)
            is Player -> {
                val style = if (skill == Skill.Ranged) if (accuracy) "_attack" else "_strength" else ""
                var bonus = character["base_${skill.name.lowercase()}${style}_bonus", 1.0]
                if (character.equipped(EquipSlot.Amulet).id == "amulet_of_zealots") {
                    bonus = floor(1.0 + (bonus - 1.0) * 2)
                }
                if (!character.contains("turmoil")) {
                    bonus += character.getLeech(skill) * 100.0 / character.levels.getMax(skill) / 100.0
                }
                bonus -= character.getBaseDrain(skill) + character.getDrain(skill) / 100.0
                bonus
            }
            else -> 1.0
        }
        val turmoil = if (character.contains("turmoil")) character["turmoil_${skill.name.lowercase()}_bonus", 0].toDouble() else 0.0
        return ((level * multiplier) + turmoil).toInt()
    }

    fun damageModifiers(
        source: Character,
        target: Character,
        type: String,
        weapon: Item,
        special: Boolean,
        damage: Int
    ): Int {
        // TODO Deflect
        if (source is NPC && usingProtectionPrayer(source, target, type)) {
            target["protected_damage"] = damage
            return 0
        } else if (source is Player && usingProtectionPrayer(source, target, type) && !hitThroughProtectionPrayer(source, target, type, weapon, special)) {
            target["protected_damage"] = damage
            return (damage * if (target is Player) 0.6 else 0.0).toInt()
        } else {
            target.clear("protected_damage")
        }
        return damage
    }

    private fun usingProtectionPrayer(source: Character, target: Character, type: String): Boolean {
        return type == "melee" && target.protectMelee() ||
                type == "range" && target.protectRange() ||
                type == "magic" && target.protectMagic() ||
                source.isFamiliar && target.protectSummoning()
    }

    fun usingDeflectPrayer(source: Character, target: Character, type: String): Boolean {
        return (type == "melee" && target.praying("deflect_melee")) ||
                (type == "range" && target.praying("deflect_missiles")) ||
                (type == "magic" && target.praying("deflect_magic")) ||
                source.isFamiliar && (target.praying("deflect_summoning"))
    }

    private fun hitThroughProtectionPrayer(source: Character, target: Character?, type: String, weapon: Item, special: Boolean): Boolean {
        if (target == null) {
            return false
        }
        if (special && weapon.id == "ancient_mace" && type == "melee") {
            return target.protectMelee()
        }
        return false
    }
}