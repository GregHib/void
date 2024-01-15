package world.gregs.voidps.world.interact.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.Prayer
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import kotlin.math.floor

object Hit {
    private val logger = InlineLogger()

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
        var chance = if (offensiveRating > defensiveRating) {
            1.0 - (defensiveRating + 2.0) / (2.0 * (offensiveRating + 1.0))
        } else {
            offensiveRating / (2.0 * (defensiveRating + 1.0))
        }

        if (Weapon.guaranteedChance(source, target, type, weapon, special)) {
            chance = 1.0
        }
        chance = Weapon.chinchompaChance(source, target, type, weapon, chance)
        if (Weapon.invalidateChance(source, target, type, weapon, special)) {
            chance = 0.0
        }
        val player = if (source is Player && source["debug", false]) source else if (target is Player && target["debug", false]) target else null
        if (player != null) {
            val style = if (type == "magic") source.spell else if (weapon.isEmpty()) "unarmed" else weapon.id
            val spec = if (source is Player && source.specialAttack) ", special" else ""
            val message = "Hit chance: $chance ($type, $style$spec)"
            player.message(message)
            logger.debug { message }
        }
        return chance
    }

    /**
     * Calculates an offensive or defensive rating for [source] against [target]
     */
    internal fun rating(source: Character, target: Character, type: String, weapon: Item, special: Boolean, offense: Boolean): Int {
        val skill = when {
            !offense && type == "magic" && target is NPC -> Skill.Magic
            !offense -> Skill.Defence
            type == "range" -> Skill.Ranged
            type == "magic" || type == "blaze" || type == "dragonfire" -> Skill.Magic
            else -> Skill.Attack
        }
        Prayer.setTurmoilTarget(source, target)
        val level = effectiveLevel(if (offense) source else target, skill, type, offense)
        val equipmentBonus = Equipment.bonus(source, target, type, offense)
        var rating = level * (equipmentBonus + 64)

        if (offense) {
            rating = Bonus.slayerModifier(source, target, type, rating, damage = false)
            rating = Weapon.specialRatingModifiers(source, type, weapon, special, rating)
        }
        if (source["debug", false]) {
            val message = "${if (offense) "Offensive" else "Defensive"} rating: $rating ($type)"
            source.message(message)
            logger.debug { message }
        }
        return rating
    }

    private fun effectiveLevel(character: Character, skill: Skill, type: String, accuracy: Boolean): Int {
        var level = character.levels.get(skill)
        if (!accuracy && type == "magic" && character is Player) {
            level = (level * 0.3 + floor(character.levels.get(Skill.Magic) * 0.7)).toInt()
        }
        level = Prayer.effectiveLevelModifier(character, skill, accuracy, level)
        if (skill == Skill.Magic && Equipment.hasVoidEffect(character)) {
            level = (level * 1.45).toInt()
        }
        level += Bonus.stance(character, skill)
        if (skill != Skill.Magic && Equipment.hasVoidEffect(character)) {
            level = (level * 1.1).toInt()
        }
        if (character["debug", false]) {
            val message = "${if (accuracy) "Accuracy" else "Damage"} effective level: $level (${skill.name.lowercase()})"
            character.message(message)
            logger.debug { message }
        }
        return level
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