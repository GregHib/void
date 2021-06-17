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
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
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

private fun getWeaponType(player: Player, weapon: Item?): String {
    if (player.spell.isNotBlank()) {
        return "spell"
    }
    return when (weapon?.def?.weaponStyle()) {
        13, 16, 17, 18, 19 -> "range"
        20 -> if (player.attackType == "aim_and_fire") "range" else "melee"
        21 -> when (player.attackType) {
            "flare" -> "range"
            "blaze" -> "blaze"
            else -> "melee"
        }
        else -> "melee"
    }
}

fun Player.hit(target: Character, weapon: Item? = this.weapon, type: String = getWeaponType(this, weapon)) {
    val damage = hit(this, target, type, weapon)
    grant(this, type, damage)
    delay(target, if (type == "melee") 0 else 2) {
        val mark = when (type) {
            "range" -> Hit.Mark.Range
            "melee" -> Hit.Mark.Melee
            "magic" -> Hit.Mark.Magic
            "dragonfire" -> Hit.Mark.Regular
            else -> Hit.Mark.Missed
        }
        events.emit(CombatDamage(target, type, damage, weapon))
        hit(this, target, damage, mark)
        target.events.emit(CombatHit(this, type, damage))
    }
}

private fun grant(player: Player, type: String, damage: Int) {
    if (type == "spell" || type == "blaze") {
        val base = 0.0
        if (player.getVar("defensive_cast", false)) {
            player.exp(Skill.Magic, base + damage / 7.5)
            player.exp(Skill.Defence, damage / 10.0)
        } else {
            player.exp(Skill.Magic, base + damage / 5.0)
        }
    } else if (type == "range") {
        if (player.attackType == "long_range") {
            player.exp(Skill.Range, damage / 5.0)
            player.exp(Skill.Defence, damage / 5.0)
        } else {
            player.exp(Skill.Range, damage / 2.5)
        }
    } else if (type == "melee") {
        when (player.attackStyle) {
            "accurate" -> player.exp(Skill.Attack, damage / 2.5)
            "aggressive" -> player.exp(Skill.Strength, damage / 2.5)
            "controlled" -> {
                player.exp(Skill.Attack, damage / 7.5)
                player.exp(Skill.Strength, damage / 7.5)
                player.exp(Skill.Defence, damage / 7.5)
            }
            "defensive" -> player.exp(Skill.Defence, damage / 2.5)
        }
    }
    player.exp(Skill.Constitution, damage / 7.5)
}

fun hit(player: Player, target: Character, damage: Int, type: Hit.Mark) {
    target.hit(player, damage, type)
    target.levels.drain(Skill.Constitution, damage)
    target["killer"] = player
    val name = (target as? NPC)?.def?.getOrNull("category") ?: "player"
    player.playSound("${name}_hit", delay = 40)
    target.setAnimation("${name}_hit")
}

fun getStrengthBonus(source: Character, type: String, weapon: Item?): Int {
    return if (type == "blaze") {
        when (weapon?.name) {
            "green_salamander" -> 56
            "orange_salamander" -> 59
            "red_salamander" -> 77
            "black_salamander" -> 92
            else -> 0
        }
    } else {
        source[if (type == "range") "range_str" else "str", 0]
    }
}

fun getMaximumHit(source: Character, target: Character? = null, type: String, weapon: Item?): Int {
    val strengthBonus = getStrengthBonus(source, type, weapon) + 64
    val baseMaxHit = if (type == "spell") {
        source["spell_damage", 0.0]
    } else {
        0.5 + (getEffectiveLevel(source, when (type) {
            "range" -> Skill.Range
            "spell", "blaze" -> Skill.Magic
            else -> Skill.Strength
        }, accuracy = false) * strengthBonus) / 64
    }
    val modifier = HitDamageModifier(target, type, strengthBonus, baseMaxHit, weapon)
    source.events.emit(modifier)
    return modifier.damage.toInt()
}

fun getMinimumHit(source: Character, target: Character? = null, type: String, weapon: Item?): Int {
    return 0
}

fun getEffectiveLevel(source: Character, skill: Skill, accuracy: Boolean): Int {
    val level = source.levels.get(skill).toDouble()
    val mod = EffectiveLevelModifier(skill, accuracy, level)
    source.events.emit(mod)
    return mod.level.toInt()
}

fun getBonus(source: Character, target: Character?, type: String, weapon: Item?): Int {
    val offense = source == target
    var level = if (target == null) 8 else getEffectiveLevel(target, when (type) {
        "range" -> Skill.Range
        "spell", "blaze" -> if (offense && target is Player) Skill.Defence else Skill.Magic
        else -> Skill.Attack
    }, offense)
    val override = HitEffectiveLevelOverride(target, type, !offense, level)
    source.events.emit(override)
    level = override.level
    val style = if (type == "range") "range" else if (type == "spell") "magic" else target?.combatStyle ?: ""
    val equipmentBonus = target?.getOrNull(if (offense) style else "${style}_def") ?: 0
    val chance = level * (equipmentBonus + 64.0)
    val modifier = HitBonusModifier(target, type, offense, chance, weapon)
    source.events.emit(modifier)
    return modifier.bonus.toInt()
}

fun hitChance(source: Character, target: Character?, type: String, weapon: Item?): Double {
    val attackerChance = getBonus(source, source, type, weapon)
    val defenderChance = getBonus(source, target, type, weapon)
    val chance = if (attackerChance > defenderChance) {
        1.0 - (defenderChance + 2.0) / (2.0 * (attackerChance + 1.0))
    } else {
        attackerChance / (2.0 * (defenderChance + 1.0))
    }

    val modifier = HitChanceModifier(target, type, chance, weapon)
    source.events.emit(modifier)
    return modifier.chance
}

fun successfulHit(source: Character, target: Character?, type: String, weapon: Item?): Boolean {
    val verac = if (source is Player) source.hasFullVeracs() else if (source is NPC) source.name == "verac" else false
    val veracs = verac && Random.nextDouble() < 0.25
    if (veracs) {
        println("Veracs")
        return true
    }

    return Random.nextDouble() < hitChance(source, target, type, weapon)
}

private fun Player.hasFullVeracs(): Boolean {
    return notBroken(equipped(EquipSlot.Hat).name, "veracs_helm") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_flail") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_brassard") &&
            notBroken(equipped(EquipSlot.Hat).name, "veracs_plateskirt")
}

private fun notBroken(name: String, prefix: String): Boolean {
    return name.startsWith(prefix) && !name.endsWith("broken")
}

fun hit(source: Character, target: Character?, type: String, weapon: Item?): Int {
    return if (successfulHit(source, target, type, weapon)) {
        val maxHit = getMaximumHit(source, target, type, weapon)
        val minHit = getMinimumHit(source, target, type, weapon)
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

val Character.spell: String
    get() = get("spell", "")

val Player.weapon: Item
    get() = get("weapon", Item.EMPTY)