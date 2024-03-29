package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.entity.combat.Equipment
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import world.gregs.voidps.world.interact.entity.player.combat.magic.spellRequiredItems
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Spell {

    fun isMultiTarget(spell: String) = spell.endsWith("_burst") || spell.endsWith("_barrage")

    fun canDrain(target: Character, spell: String): Boolean {
        val def = get<SpellDefinitions>().get(spell)
        val skill = Skill.valueOf(def["drain_skill"])
        val multiplier: Double = def["drain_multiplier"]
        val maxDrain = multiplier * target.levels.getMax(skill)
        return target.levels.getOffset(skill) > -maxDrain
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

    fun removeRequirements(player: Player, spell: String): Boolean {
        val runes = mutableListOf<Item>()
        val items = mutableListOf<Item>()
        if (!hasRequirements(player, spell, runes, items)) {
            return false
        }
        for (rune in runes) {
            player.inventory.remove(rune.id, rune.amount)
        }
//        for (rune in items) {
//            if (rune.id.endsWith("_staff")) {
//                val staff = player.equipped(EquipSlot.Weapon)
//                staff.charge = (staff.charge - rune.amount).coerceAtLeast(0)
//            } else {
//            }
//        }
        return true
    }

    fun hasRequirements(player: Player, spell: String, runes: MutableList<Item> = mutableListOf(), items: MutableList<Item> = mutableListOf()): Boolean {
        val component = get<InterfaceDefinitions>().getComponent(player.spellBook, spell) ?: return false
        if (!player.has(Skill.Magic, component.magicLevel, message = true)) {
            return false
        }
        for (item in component.spellRequiredItems()) {
            if (!Runes.hasRunes(player, item, runes, items)) {
                player.message("You do not have the required items to cast this spell.")
                return false
            }
        }
        return true
    }

    private val InterfaceComponentDefinition.magicLevel: Int
        get() = requiredItems?.getOrNull(5) as? Int ?: 0

    private val InterfaceComponentDefinition.prettyName: String
        get() = requiredItems?.getOrNull(6) as? String ?: ""
}

var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"