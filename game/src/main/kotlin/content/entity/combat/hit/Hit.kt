package content.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import content.entity.combat.Bonus
import content.entity.combat.dead
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random
import content.skill.magic.spell.spell
import content.skill.prayer.Prayer
import content.entity.player.combat.special.specialAttack
import content.entity.player.equip.Equipment
import content.skill.melee.weapon.Weapon
import content.skill.melee.weapon.weapon
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
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
}

/**
 * Hits player during combat
 * @param delay Hit delay in client ticks
 */
fun Character.hit(
    target: Character,
    weapon: Item = this.weapon,
    type: String = Weapon.type(this, weapon),
    mark: HitSplat.Mark = Weapon.mark(this, type),
    delay: Int = if (type == "melee") 0 else 64,
    spell: String = this.spell,
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    damage: Int = Damage.roll(this, target, type, weapon, spell)
): Int {
    val actualDamage = Damage.modify(this, target, type, damage, weapon, spell, special)
        .coerceAtMost(target.levels.get(Skill.Constitution))
    emit(CombatAttack(target, type, mark, actualDamage, weapon, spell, special, delay))
    target.strongQueue("hit", if (delay == 0) 0 else CLIENT_TICKS.toTicks(delay) + 1) {
        target.directHit(this@hit, actualDamage, type, mark, weapon, spell, special)
    }
    return actualDamage
}

/**
 * Hits player without interrupting them
 */
fun Character.directHit(damage: Int, type: String = "damage", mark: HitSplat.Mark = HitSplat.Mark.Regular, weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false, source: Character = this) =
    directHit(source, damage, type, mark, weapon, spell, special)

/**
 * Hits player without interrupting them
 */
fun Character.directHit(source: Character, damage: Int, type: String = "damage", mark: HitSplat.Mark = HitSplat.Mark.Regular, weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    emit(CombatDamage(source, type, mark, damage, weapon, spell, special))
    if (source["debug", false] || this["debug", false]) {
        val player = if (this["debug", false] && this is Player) this else source as Player
        val message = "Damage: $damage ($type, $mark, ${if (weapon.isEmpty()) "unarmed" else weapon.id})"
        player.message(message)
    }
}