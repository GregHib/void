package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.Weapon

object Damage {
    /**
     * Rolls a real hit against [target] without modifiers
     * @return damage or -1 if unsuccessful
     */
    fun roll(source: Character, target: Character, type: String, weapon: Item, spell: String = "", special: Boolean = false): Int {
        if (!Hit.success(source, target, type, weapon, special)) {
            return -1
        }
        val baseMaxHit = maximum(source, type, weapon, spell)
        source["max_hit"] = baseMaxHit
        return random.nextInt(baseMaxHit + 1)
    }

    /**
     * Calculates the base maximum damage before modifications are applied
     */
    fun maximum(source: Character, type: String, weapon: Item, spell: String = ""): Int {
        val strengthBonus = Weapon.strengthBonus(source, type, weapon)
        if (source is NPC) {
            return source.def["max_hit_$type", 0]
        }
        if (type == "magic") {
            val damage = get<SpellDefinitions>().get(spell).maxHit
            return if (damage == -1) 0 else damage
        }

        val skill = when (type) {
            "range" -> Skill.Ranged
            "blaze" -> Skill.Magic
            else -> Skill.Strength
        }
        return 5 + (Hit.effectiveLevel(source, skill, accuracy = false) * strengthBonus) / 64
    }

    /**
     * Applies modifiers to a [maximum]
     */
    fun modify(source: Character, target: Character, type: String, baseMaxHit: Int, weapon: Item, spell: String = "", special: Boolean = false): Int {
        val modifier = HitDamageModifier(target, type, baseMaxHit, weapon, spell, special)
        source.events.emit(modifier)
        return modifier.damage
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