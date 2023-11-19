package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.random

object Damage {

    /**
     * Rolls a hit against [target]
     * @return damage or -1 if unsuccessful
     */
    fun roll(source: Character, target: Character? = null, type: String, weapon: Item? = null, spell: String = "", special: Boolean = false): Int {
        if (!success(source, target, type, weapon, special)) {
            return -1
        }
        val strengthBonus = Weapon.strengthBonus(source, type, weapon)
        val baseMaxHit = maximum(source, type, spell, strengthBonus)
        val damage = random.nextInt(baseMaxHit.toInt() + 1).toDouble()
        return modify(source, target, type, strengthBonus, damage, weapon, spell, special)
    }

    /**
     * @return true if [chance] of hitting was successful
     */
    fun success(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean): Boolean {
        return random.nextDouble() < chance(source, target, type, weapon, special)
    }

    /**
     * @return chance between 0.0 and 1.0 of hitting [target]
     */
    fun chance(source: Character, target: Character? = null, type: String, weapon: Item? = null, special: Boolean = false): Double {
        val offensiveRating = getRating(source, target, type, weapon, special, true)
        val defensiveRating = getRating(source, target, type, weapon, special, false)
        val chance = if (offensiveRating > defensiveRating) {
            1.0 - (defensiveRating + 2.0) / (2.0 * (offensiveRating + 1.0))
        } else {
            offensiveRating / (2.0 * (defensiveRating + 1.0))
        }
        val modifier = HitChanceModifier(target, type, chance, weapon, special)
        source.events.emit(modifier)
        return modifier.chance
    }

    /**
     * The absolute maximum damage [source] can do against [target]
     */
    fun maximum(source: Character, target: Character? = null, type: String, weapon: Item? = null, spell: String = "", special: Boolean = false): Int {
        val strengthBonus = Weapon.strengthBonus(source, type, weapon)
        val baseMaxHit = maximum(source, type, spell, strengthBonus)
        return modify(source, target, type, strengthBonus, baseMaxHit, weapon, spell, special)
    }

    /**
     * Calculates the base maximum damage before modifications are applied
     */
    private fun maximum(source: Character, type: String, spell: String, strengthBonus: Int): Double {
        if (source is NPC) {
            return source.def["max_hit_$type", 0].toDouble()
        }
        if (type == "magic") {
            val damage = get<SpellDefinitions>().get(spell).maxHit
            return if (damage == -1) 0.0 else damage.toDouble()
        }

        val skill = when (type) {
            "range" -> Skill.Ranged
            "blaze" -> Skill.Magic
            else -> Skill.Strength
        }
        return 5.0 + (getEffectiveLevel(source, skill, accuracy = false) * strengthBonus) / 64
    }

    private fun modify(source: Character, target: Character?, type: String, strengthBonus: Int, baseMaxHit: Double, weapon: Item?, spell: String, special: Boolean): Int {
        val modifier = HitDamageModifier(target, type, strengthBonus, baseMaxHit, weapon, spell, special)
        source.events.emit(modifier)
        source["max_hit"] = modifier.damage.toInt()
        return modifier.damage.toInt()
    }
}