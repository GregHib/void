package content.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import content.entity.combat.Bonus
import content.entity.combat.dead
import content.entity.player.combat.special.specialAttack
import content.entity.player.equip.Equipment
import content.skill.magic.spell.spell
import content.skill.melee.weapon.Weapon
import content.skill.melee.weapon.weapon
import content.skill.prayer.Prayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random
import kotlin.math.floor

object Hit {
    private val logger = InlineLogger()

    /**
     * @return true if [chance] of hitting was successful
     */
    fun success(source: Character, target: Character, offensiveType: String, weapon: Item, special: Boolean, defensiveType: String = offensiveType): Boolean = random.nextDouble() < chance(source, target, offensiveType, weapon, special, defensiveType)

    /**
     * @return chance between 0.0 and 1.0 of hitting [target]
     */
    fun chance(
        source: Character,
        target: Character,
        offensiveType: String,
        weapon: Item,
        special: Boolean = false,
        defenceType: String = offensiveType,
    ): Double {
        val offensiveRating = rating(source, target, offensiveType, weapon, special, true)
        val defensiveRating = rating(source, target, defenceType, weapon, special, false)
        var chance = if (offensiveRating > defensiveRating) {
            1.0 - (defensiveRating + 2.0) / (2.0 * (offensiveRating + 1.0))
        } else {
            offensiveRating / (2.0 * (defensiveRating + 1.0))
        }
        if (Weapon.guaranteedChance(source, target, offensiveType, weapon, special)) {
            chance = 1.0
        }
        chance = Weapon.chinchompaChance(source, target, offensiveType, weapon, chance)
        if (Weapon.invalidateChance(source, target, offensiveType, weapon, special)) {
            chance = 0.0
        }
        val player = if (source is Player && source["debug", false]) {
            source
        } else if (target is Player && target["debug", false]) {
            target
        } else {
            null
        }
        if (player != null) {
            val style = if (offensiveType == "magic") {
                source.spell
            } else if (weapon.isEmpty()) {
                "unarmed"
            } else {
                weapon.id
            }
            val spec = if (source is Player && source.specialAttack) ", special" else ""
            val message = "Hit chance: $chance ($offensiveType, $style$spec)"
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
        } else if (target["debug", false]) {
            val message = "${if (offense) "Offensive" else "Defensive"} rating: $rating ($type)"
            target.message(message)
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
 * Hit a character during combat
 * @param target The target to hit
 * @param weapon The weapon used in the attack
 * @param offensiveType attack type used for calculating offensive rating and damage
 * @param defensiveType attack type used for rolling the [target]s defensive rating
 * @param delay Hit delay in client ticks
 * @param spell The type of maigc spell used
 * @param special Special attack
 * @param damage The amount of damage dealt
 * @return The actual amount damage dealt after bonuses and protections applied
 */
fun Character.hit(
    target: Character,
    weapon: Item = this.weapon,
    offensiveType: String = Weapon.type(this, weapon),
    delay: Int = if (offensiveType == "melee") 0 else 64,
    spell: String = this.spell,
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    defensiveType: String = offensiveType,
    damage: Int = Damage.roll(this, target, offensiveType, weapon, spell, special, defensiveType),
): Int {
    val actualDamage = Damage.modify(this, target, offensiveType, damage, weapon, spell, special)
        .coerceAtMost(target.levels.get(Skill.Constitution))
    if (this is Player) {
        CombatApi.attack(this, CombatAttack(target, actualDamage, offensiveType, weapon, spell, special, delay))
    } else if (this is NPC) {
        CombatApi.attack(this, CombatAttack(target, actualDamage, offensiveType, weapon, spell, special, delay))
    }
    target.strongQueue("hit", if (delay == 0) 0 else CLIENT_TICKS.toTicks(delay) + 1) {
        target.directHit(this@hit, actualDamage, offensiveType, weapon, spell, special)
    }
    return actualDamage
}

/**
 * Hits player without interrupting them
 */
fun Character.directHit(damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false, source: Character = this) = directHit(source, damage, type, weapon, spell, special)

/**
 * Hits player without interrupting them
 */
fun Character.directHit(source: Character, damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    emit(CombatDamage(source, type, damage, weapon, spell, special))
    if (source["debug", false] || this["debug", false]) {
        val player = if (this["debug", false] && this is Player) this else source as Player
        val message = "Damage: $damage ($type, ${if (weapon.isEmpty()) "unarmed" else weapon.id})"
        player.message(message)
    }
}
