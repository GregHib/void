package world.gregs.voidps.world.interact.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.Prayer
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
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

        if (type == "melee" && source.contains("veracs_set_effect") && random.nextInt(4) == 0) {
            chance = 1.0
            target.start("veracs_effect", 1)
        } else if (special && weapon.id.startsWith("bone_dagger")) {
            val last = target.attackers.lastOrNull()
            if (last != source) {
                chance = 1.0
            }
        } else if (type == "magic" && special && weapon.id == "korasis_sword") {
            chance = 1.0
        } else if (type == "range" && special && (weapon.id.startsWith("magic_longbow") || weapon.id.startsWith("magic_composite_bow"))) {
            chance = 1.0
        } else if (type == "range" && special && weapon.id == "seercull") {
            chance = 1.0
        } else if (type == "range" && source != target && weapon.id.endsWith("chinchompa")) {
            val distance = source.tile.distanceTo(target)
            chance = when (source.attackType) {
                "short_fuse" -> when {
                    distance <= 3 -> 1.0
                    distance <= 6 -> 0.75
                    else -> 0.5
                }
                "medium_fuse" -> when {
                    distance <= 3 -> 0.75
                    distance <= 6 -> 1.0
                    else -> 0.75
                }
                "long_fuse" -> when {
                    distance <= 3 -> 0.5
                    distance <= 6 -> 0.75
                    else -> 1.0
                }
                else -> 0.0
            }
        }
        if (type == "melee" && target.hasClock("spear_wall")) {
            chance = 0.0
        }
        if (source["debug", false]) {
            val style = if (type == "magic") source.spell else if (weapon.isEmpty()) "unarmed" else weapon.id
            val spec = if (source is Player && source.specialAttack) ", special" else ""
            val message = "Hit chance: $chance ($type, $style$spec)"
            source.message(message)
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
            type == "magic" || type == "blaze" -> Skill.Magic
            else -> Skill.Attack
        }
        val level = effectiveLevel(if (offense) source else target, skill, type, offense)
        val equipmentBonus = Equipment.bonus(source, target, type, offense)
        var rating = level * (equipmentBonus + 64)

        if (offense) {
            if (source is Player) {
                rating = (rating * Bonus.slayer(source, target, type, false)).toInt()
            }
            if (Weapon.isDemonbane(weapon) && Target.isDemon(target)) {
                rating = (rating * 1.2).toInt()
            }
            if (type == "melee" && special) {
                rating = (rating * weapon.def["special_accuracy_mod", 1.0]).toInt()
            }
            if (type == "melee" && special && weapon.id == "dragon_halberd" && source["second_hit", false]) {
                rating = (rating * 0.75).toInt()
            }
        } else {
            if (source.praying("turmoil")) {
                if (!source["turmoil", false]) {
                    source.toggle("turmoil")
                }
                source["turmoil_attack_bonus"] = (target.levels.get(Skill.Attack).coerceAtMost(99) * 0.15).toInt()
                source["turmoil_strength_bonus"] = (target.levels.get(Skill.Strength).coerceAtMost(99) * 0.10).toInt()
                source["turmoil_defence_bonus"] = (target.levels.get(Skill.Defence).coerceAtMost(99) * 0.15).toInt()
            } else if (!source.praying("turmoil") && source["turmoil", false]) {
                source.toggle("turmoil")
            }
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
    val actualDamage = Damage.modify(this, target, type, damage, weapon, special)
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