package world.gregs.voidps.world.interact.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.Bonus
import world.gregs.voidps.world.interact.entity.combat.Equipment
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.Weapon
import world.gregs.voidps.world.interact.entity.player.combat.armour.barrows.BarrowsArmour
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import content.skill.prayer.Prayer
import content.entity.player.combat.special.specialAttack
import content.entity.player.effect.Dragonfire

object Damage {
    private val logger = InlineLogger()

    /**
     * Rolls a real hit against [target] without modifiers
     * @return damage or -1 if unsuccessful
     */
    fun roll(source: Character, target: Character, type: String, weapon: Item, spell: String = "", special: Boolean = false): Int {
        val success = Hit.success(source, target, type, weapon, special)
        if (type != "dragonfire" && !success) {
            return -1
        }
        val baseMaxHit = maximum(source, target, type, weapon, spell, success)
        source["max_hit"] = baseMaxHit
        val player = if (source is Player && source["debug", false]) source else if (target is Player && target["debug", false]) target else null
        if (player != null) {
            val message = "Base maximum hit: $baseMaxHit ($type, ${if (weapon.isEmpty()) "unarmed" else weapon.id})"
            player.message(message)
            logger.debug { message }
        }
        return random.nextInt(baseMaxHit + 1)
    }

    /**
     * Calculates the base maximum damage before modifications are applied
     * @param target only applicable for "dragonfire" [type]
     * @param special only applicable for "dragonfire" type
     */
    fun maximum(source: Character, target: Character, type: String, weapon: Item, spell: String = "", special: Boolean = false): Int = when {
        type == "dragonfire" -> Dragonfire.maxHit(source, target, special || source is NPC && spell != "")
        source is NPC -> source.def["max_hit_$type", 0]
        type == "magic" && weapon.id.startsWith("saradomin_sword") -> 160
        type == "magic" && spell == "magic_dart" -> effectiveLevel(source, Skill.Magic) + 100
        type == "magic" -> {
            var damage = get<SpellDefinitions>().get(spell).maxHit
            if (damage == -1) {
                damage = 0
            }
            if (source is Player && spell.endsWith("_bolt") && source.equipped(EquipSlot.Hands).id == "chaos_gauntlets") {
                damage += 30
            }
            damage
        }
        else -> {
            val skill = when (type) {
                "range" -> Skill.Ranged
                "blaze" -> Skill.Magic
                else -> Skill.Strength
            }
            val strengthBonus = Weapon.strengthBonus(source, type, weapon)
            5 + (effectiveLevel(source, skill) * strengthBonus) / 64
        }
    }

    private fun effectiveLevel(character: Character, skill: Skill): Int {
        var level = character.levels.get(skill)
        if (skill != Skill.Magic) {
            level = Prayer.effectiveLevelModifier(character, skill, false, level)
        }
        if (skill != Skill.Magic) {
            level += Bonus.stance(character, skill)
        }
        level = Equipment.voidEffectiveLevelModifier(skill, character, level)
        if (character["debug", false]) {
            val message = "Damage effective level: $level (${skill.name.lowercase()})"
            character.message(message)
            logger.debug { message }
        }
        return level
    }

    /**
     * Applies modifiers to a [maximum]
     */
    fun modify(source: Character, target: Character, type: String, baseMaxHit: Int, weapon: Item, spell: String, special: Boolean = false): Int {
        var damage = baseMaxHit

        damage = Spell.damageModifiers(source, type, weapon, spell, damage)

        damage = Bonus.slayerModifier(source, target, type, damage, damage = true)

        damage = Weapon.weaponDamageModifiers(source, target, type, weapon, special, damage)

        damage = Equipment.damageModifiers(source, target, type, damage)

        damage = Weapon.specialDamageModifiers(weapon, special, damage)

        damage = Prayer.damageModifiers(source, target, type, weapon, special, damage)

        damage = Target.damageReductionModifiers(source, target, damage)

        damage = BarrowsArmour.damageModifiers(source, target, weapon, damage)

        damage = Equipment.shieldDamageReductionModifiers(source, target, type, damage)

        damage = Target.damageLimitModifiers(target, damage)

        if (source["debug", false]) {
            val strengthBonus = Weapon.strengthBonus(source, type, weapon)
            val style = if (type == "magic") source.spell else if (weapon.isEmpty()) "unarmed" else weapon.id
            val spec = if ((source as? Player)?.specialAttack == true) "special" else ""
            val message = "Max damage: $damage (${listOf(type, "$strengthBonus str", style, spec).joinToString(", ")})"
            source.message(message)
            logger.debug { message }
        }
        return damage
    }

}

/**
 * Damages player closing any interfaces they have open
 */
fun Character.damage(damage: Int, delay: Int = 0, type: String = "damage") {
    strongQueue("hit", delay) {
        directHit(damage, type)
    }
}