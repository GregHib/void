package content.skill.magic.spell

import content.entity.player.equip.Equipment
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Spell {

    fun canDrain(target: Character, spell: String): Boolean {
        val def = get<SpellDefinitions>().get(spell)
        return canDrain(target, def)
    }

    fun canDrain(target: Character, def: SpellDefinition): Boolean {
        val skill = Skill.valueOf(def["drain_skill"])
        val multiplier: Double = def["drain_multiplier"]
        val maxDrain = multiplier * target.levels.getMax(skill)
        return target.levels.getOffset(skill) > -maxDrain
    }

    fun drain(source: Character, target: Character, spell: String) {
        val def = get<SpellDefinitions>().get(spell)
        val multiplier: Double = def["drain_multiplier"]
        val skill = Skill.valueOf(def["drain_skill"])
        val drained = target.levels.drain(skill, multiplier = multiplier, stack = target is Player)
        if (target.levels.getOffset(skill).absoluteValue >= multiplier * 100 && drained == 0) {
            source.message("The spell has no effect because the target has already been weakened.")
        } else {
            target.message("You feel slightly weakened.", ChatType.Filter)
        }
    }

    /**
     * Applies modifications to spells damage
     */
    fun damageModifiers(source: Character, type: String, weapon: Item, spell: String, baseDamage: Int): Int {
        if (type != "magic") {
            return baseDamage
        }
        var damage = baseDamage
        val magicDamage = weapon.def["magic_damage", 0]
        if (magicDamage > 0 || Equipment.hasEliteVoidEffect(source)) {
            val equipmentDamage = magicDamage / 100.0
            val eliteVoidDamage = if (Equipment.hasEliteVoidEffect(source)) 0.025 else 0.0
            val damageMultiplier = 1.0 + equipmentDamage + eliteVoidDamage
            damage = (damage * damageMultiplier).roundToInt()
        }
        if (source.hasClock("charge") && source is Player && Equipment.wearingMatchingArenaGear(source, spell)) {
            damage += 100
        }
        return damage
    }
}

var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"
