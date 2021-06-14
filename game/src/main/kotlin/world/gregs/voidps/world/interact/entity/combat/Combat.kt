package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.world.interact.entity.player.equip.weaponStyle
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random
import kotlin.random.nextInt

/*
    Combat lifecycle

    A) calculate chance
        equipmentBonus
        effectiveLevel
            level
            bonus
            stance + 8
            EffectiveLevelModifier/void
        EffectiveOverride
        calculate chance
        HitChanceModifier (e.g slayer task boosts, special attack)

    1. start swing
    2. calculate attacker chance (A)
    3. calculate defender chance (A)
    4. roll chances
    5. calculate max hit
        strength bonus
        base max hit
        DamageBaseModifier (slayer tasks, special pt 1)
        protection prayers
        DamageModifier  (passive effects, special pt 2, area effects)
    6. roll damage
    7. apply spirit shield modifiers?
    10. end swing


    goal

    player.hit(target, weapon (or null for spell))
 */

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") as? Int ?: ShootProjectile.DEFAULT_HEIGHT


private fun getWeaponSkill(player: Player, weapon: Item?): Skill {
    if (player.spell.isNotBlank()) {
        return Skill.Magic
    }
    return when (weapon?.def?.weaponStyle()) {
        13, 16, 17, 18, 19 -> Skill.Range
        20 -> if (player.attackType == "aim_and_fire") Skill.Range else Skill.Attack
        21 -> when (player.attackType) {
            "flare" -> Skill.Range
            "blaze" -> Skill.Magic
            else -> Skill.Attack
        }
        else -> Skill.Attack
    }
}

fun Player.hit(target: Character, weapon: Item?, skill: Skill = getWeaponSkill(this, weapon)) {
    val damage = hit(this, target, skill, weapon)
    if (skill == Skill.Magic) {
        val base = 0.0
        if (getVar("defensive_cast", false)) {
            exp(skill, base + damage / 7.5)
            exp(Skill.Defence, damage / 10.0)
        } else {
            exp(skill, base + damage / 5.0)
        }
    } else if (skill == Skill.Range) {
        if (attackType == "long_range") {
            exp(skill, damage / 5.0)
            exp(Skill.Defence, damage / 5.0)
        } else {
            exp(skill, damage / 2.5)
        }
    } else if (skill == Skill.Attack) {
        when (attackStyle) {
            "accurate" -> exp(skill, damage / 2.5)
            "aggressive" -> exp(Skill.Strength, damage / 2.5)
            "controlled" -> {
                exp(Skill.Attack, damage / 7.5)
                exp(Skill.Strength, damage / 7.5)
                exp(Skill.Defence, damage / 7.5)
            }
            "defensive" -> exp(Skill.Defence, damage / 2.5)
        }
    }
    exp(Skill.Constitution, damage / 7.5)
    delay(target, if (skill == Skill.Attack) 0 else 2) {
        val mark = when (skill) {
            Skill.Range -> Hit.Mark.Range
            Skill.Attack -> Hit.Mark.Melee
            Skill.Magic -> Hit.Mark.Magic
            else -> Hit.Mark.Missed
        }
        hit(this, target, damage, mark)
    }
}

fun hit(player: Player, target: Character, damage: Int, type: Hit.Mark) {
    target.hit(player, damage, type)
    target.levels.drain(Skill.Constitution, damage)
    target["killer"] = player
    val name = (target as? NPC)?.def?.getOrNull("category") ?: "player"
    player.playSound("${name}_hit", delay = 40)
    target.setAnimation("${name}_hit")
}


fun usingSalamander(source: Character, weapon: Item?): Boolean = !source.contains("spell") && weapon?.name?.endsWith("salamander") == true

fun getStrengthBonus(source: Character, skill: Skill, weapon: Item?): Int {
    return if (skill == Skill.Magic && usingSalamander(source, weapon)) {
        when (weapon?.name) {
            "green_salamander" -> 56
            "orange_salamander" -> 59
            "red_salamander" -> 77
            "black_salamander" -> 92
            else -> 0
        }
    } else {
        source[if (skill == Skill.Range) "range_str" else "str", 0]
    }
}

fun getMaximumHit(source: Character, target: Character? = null, skill: Skill, weapon: Item?): Int {
    val strengthBonus = getStrengthBonus(source, skill, weapon) + 64
    val baseMaxHit = if (skill == Skill.Magic && !usingSalamander(source, weapon)) {
        source["spell_damage", 0.0]
    } else {
        0.5 + (getEffectiveLevel(source, skill, accuracy = false) * strengthBonus) / 64
    }
    val modifier = HitDamageModifier(target, skill, strengthBonus, baseMaxHit, weapon)
    source.events.emit(modifier)
    return modifier.damage.toInt()
}

fun getMinimumHit(source: Character, target: Character? = null, skill: Skill, weapon: Item?): Int {
    return 0
}

fun getEffectiveLevel(source: Character, skill: Skill, accuracy: Boolean): Int {
    val level = source.levels.get(skill).toDouble()
    val mod = EffectiveLevelModifier(skill, accuracy, level)
    source.events.emit(mod)
    return mod.level.toInt()
}

fun getChance(source: Character, target: Character?, skill: Skill, weapon: Item?): Int {
    val offense = source == target
    var level = if (target == null) 8 else getEffectiveLevel(target, if (skill == Skill.Magic && offense && target is Player) Skill.Defence else skill, offense)
    val override = HitChanceLevelOverride(target, skill, !offense, level)
    source.events.emit(override)
    level = override.level
    val style = if (skill == Skill.Range) "range" else if (skill == Skill.Magic) "magic" else target?.combatStyle ?: ""
    val equipmentBonus = target?.getOrNull(if (offense) style else "${style}_def") ?: 0
    val chance = level * (equipmentBonus + 64.0)
    val modifier = HitChanceModifier(target, skill, offense, chance, weapon)
    source.events.emit(modifier)
    return modifier.chance.toInt()
}

fun hitChance(source: Character, target: Character?, skill: Skill, weapon: Item?): Double {
    val attackerChance = getChance(source, source, skill, weapon)
    val defenderChance = getChance(source, target, skill, weapon)
    return if (attackerChance > defenderChance) {
        1.0 - (defenderChance + 2.0) / (2.0 * (attackerChance + 1.0))
    } else {
        attackerChance / (2.0 * (defenderChance + 1.0))
    }
}

fun hit(player: Player, target: Character?, skill: Skill, weapon: Item?): Int {
    val chance = hitChance(player, target, skill, weapon)
    return if (Random.nextDouble() < chance) {
        val maxHit = getMaximumHit(player, target, if (skill == Skill.Attack) Skill.Strength else skill, weapon)
        val minHit = getMinimumHit(player, target, if (skill == Skill.Attack) Skill.Strength else skill, weapon)
        Random.nextInt(minHit..maxHit)
    } else {
        0
    }
}

val ItemDefinition.ammo: Set<String>?
    get() = (getOrNull("ammo") as? ArrayList<String>)?.toSet()

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

val Player.specialAttack: Boolean
    get() = getVar("special_attack", false)

val Character.spell: String
    get() = get("spell", "")