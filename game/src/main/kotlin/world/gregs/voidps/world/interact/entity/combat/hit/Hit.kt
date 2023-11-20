package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

object Hit {

    /**
     * @return true if [chance] of hitting was successful
     */
    fun success(source: Character, target: Character, type: String, weapon: Item, special: Boolean): Boolean {
        return random.nextDouble() < chance(source, target, type, weapon, special)
    }

    /**
     * @return chance between 0.0 and 1.0 of hitting [target]
     */
    fun chance(source: Character, target: Character, type: String, weapon: Item, special: Boolean = false): Double {
        val offensiveRating = rating(source, target, type, weapon, special, true)
        val defensiveRating = rating(source, target, type, weapon, special, false)
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
     * Calculates an offensive or defensive rating for [source] against [target]
     */
    internal fun rating(source: Character, target: Character, type: String, weapon: Item, special: Boolean, offense: Boolean): Int {
        val skill = when {
            !offense && type != "magic" -> Skill.Defence
            type == "range" -> Skill.Ranged
            type == "magic" || type == "blaze" -> Skill.Magic
            else -> Skill.Attack
        }
        val level = effectiveLevel(if (offense) source else target, skill, offense)
        val equipmentBonus = Equipment.bonus(source, target, type, offense)
        val rating = level * (equipmentBonus + 64)
        val modifier = HitRatingModifier(target, type, offense, rating, weapon, special)
        source.events.emit(modifier)
        return modifier.rating
    }

    fun effectiveLevel(character: Character, skill: Skill, accuracy: Boolean): Int {
        val level = character.levels.get(skill)
        val modifier = HitEffectiveLevelModifier(skill, accuracy, level)
        character.events.emit(modifier)
        return modifier.level
    }

    fun bowDelay(distance: Int) = 1 + (distance + 3) / 6

    fun throwDelay(distance: Int) = 1 + distance / 6

    fun magicDelay(distance: Int) = 1 + (distance + 1) / 3

    fun darkBowDelay(distance: Int) = 1 + (distance + 2) / 3

    fun dfsDelay(distance: Int) = 2 + (distance + 4) / 6
}

/**
 * Hits player during combat
 */
fun Character.hit(
    target: Character,
    weapon: Item = this.weapon,
    type: String = Weapon.type(this, weapon),
    delay: Int = if (type == "melee") 0 else 2,
    spell: String = this.spell,
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    damage: Int = Damage.roll(this, target, type, weapon, spell)
): Int {
    val actualDamage = Damage.modify(this, target, type, damage, weapon, spell, special)
        .coerceAtMost(target.levels.get(Skill.Constitution))
    events.emit(CombatAttack(target, type, actualDamage, weapon, spell, special, TICKS.toClientTicks(delay)))
    target.strongQueue("hit", delay) {
        target.directHit(this@hit, actualDamage, type, weapon, spell, special)
    }
    return actualDamage
}

/**
 * Hits player without interrupting them
 */
fun Character.directHit(damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false, source: Character = this) =
    directHit(source, damage, type, weapon, spell, special)

/**
 * Hits player without interrupting them
 */
fun Character.directHit(source: Character, damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    events.emit(CombatHit(source, type, damage, weapon, spell, special))
}