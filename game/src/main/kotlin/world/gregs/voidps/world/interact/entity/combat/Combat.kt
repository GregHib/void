package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
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
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.world.activity.skill.slayer.hasSlayerTask
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.activity.skill.slayer.isUndead
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.floor
import kotlin.random.Random

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") as? Int ?: ShootProjectile.DEFAULT_HEIGHT

fun rangeHit(player: Player, target: Character, damage: Int = Random.nextInt(100 + 1).coerceAtLeast(0)) {
    player.exp(Skill.Range, if (player.attackType == "long_range") damage * 0.2 else damage * 0.4)
    player.exp(Skill.Defence, if (player.attackType == "long_range") damage * 0.2 else 0.0)
    player.exp(Skill.Constitution, damage * 0.199)
    delay(target, 2) {
        hit(player, target, damage, Hit.Mark.Range)
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

fun applyBaseDamage(source: Character, target: Character?, skill: Skill, baseDamage: Double, strengthBonus: Int): Double {
    var maxHit = baseDamage
    if (skill == Skill.Magic && source.hasEffect("charge")) {
        maxHit += 100.0// DamageBaseModifier High
    }
    if (source is Player) {
        // DamageBaseModifier Med
        maxHit = floor(maxHit * source.getSlayerMultiplier(skill, target, true))
        // DamageBaseModifier Low
        val special = source.getVar("special_attack", false)
        val weapon = source.equipped(EquipSlot.Weapon)
        if (special && skill == Skill.Strength) {
            when {
                // TODO damage_multiplier special
                weapon.name.endsWith("godsword") || weapon.name == "saradomin_sword" || weapon.name == "barrelchest_anchor" || weapon.name == "dragon_halberd" -> maxHit = floor(maxHit * 1.10)
                weapon.name.startsWith("dragon_dagger") || weapon.name == "rune_claws" -> maxHit = floor(maxHit * 1.15)
                weapon.name == "dragon_longsword" -> maxHit = floor(maxHit * 1.25)
                weapon.name == "dragon_mace" -> maxHit = floor(maxHit * 1.50)
            }
        }
        if (skill == Skill.Range) {
            if (special && weapon.name == "dark_bow") {
                maxHit = floor(maxHit * if (source.equipped(EquipSlot.Ammo).name == "dragon_arrow") 1.50 else 1.30)
            } else if (isWeaponOutlier(special, weapon.name)) {
                maxHit = 0.5 + (source.levels.get(skill) + 10) * strengthBonus / 640
                if (weapon.name == "rune_thrownaxe" || (weapon.name == "magic_shortbow" && target is Player)) {
                    maxHit += 1.0
                }
            } else if (source.hasEffect("armour_piercing")) {
                maxHit = floor(maxHit * 1.15)
            } else if (source.hasEffect("life_leech") && !isUndead(target)) {
                maxHit = floor(maxHit * 1.20)
            }
        }
    }
    return maxHit
}

/*
DamageModifier
EffectiveLevelModifier
EffectiveLevel
ChanceModifier
 */
/**
 * Max hit - post protection prayers
 */
data class DamageModifier(val target: Character?, val skill: Skill, var damage: Double) : Event
/**
 * Max hit - pre protection prayers
 */
data class DamageBaseModifier(val target: Character?, val skill: Skill, val strengthBonus: Int, var damage: Double) : Event

/**
 * Post effective level - aka void for both chance and max high calcs
 */
data class EffectiveLevelModifier(val skill: Skill, val accuracy: Boolean, var level: Double) : Event

/**
 * EffectiveOverride for chance calcs
 */
data class EffectiveLevelOverride(val target: Character?, val skill: Skill, val defence: Boolean, var level: Double) : Event

/**
 * Hit chance modifier
 */
data class ChanceModifier(val target: Character?, val skill: Skill, val offense: Boolean, var chance: Double) : Event


fun applyDamageModifier(source: Character, target: Character?, skill: Skill, baseDamage: Double): Double {
    var maxHit = baseDamage
    if (source is Player) {
        val special = source.getVar("special_attack", false)
        val weapon = source.equipped(EquipSlot.Weapon)
        // DamageModifier Med
        if (skill == Skill.Range) {
            when {
                source.hasEffect("lucky_lightning") -> maxHit += floor(source.levels.get(skill) * 0.1)
                source.hasEffect("sea_curse") && !isWatery(target) -> maxHit += floor(source.levels.get(skill) * if (isFirey(target)) 1.0 / 15.0 else 0.05)
                source.hasEffect("dragons_breath") && !isFirey(target) && target?.hasEffect("anti-fire") != true -> maxHit *= floor(source.levels.get(skill) * 0.2)
                source.hasEffect("blood_forfeit") -> maxHit = floor(source.levels.get(Skill.Constitution) * 0.2)
                special && weapon.name.endsWith("morrigans_throwing_axe") -> maxHit = floor(maxHit * 1.2)
            }
        } else if (skill == Skill.Strength) {
            when {
                special && weapon.name == "armadyl_godsword" -> maxHit = floor(maxHit * 1.25)
                special && weapon.name == "bandos_godsword" -> maxHit = floor(maxHit * 1.1)
                isTzhaarWeapon(weapon) && source.equipped(EquipSlot.Amulet).name == "berserker_necklace" -> maxHit = floor(maxHit * 1.20)
                weapon.name == "dark_light" && isDemon(target) -> maxHit = floor(maxHit * 1.60)
                weapon.name == "keris" && isKalphite(target) -> maxHit = floor(maxHit * if (Random.nextDouble() < 0.51) 3.0 else 1.0 + 1.0 / 3.0)
                weapon.name == "gadderhammer" && isShade(target) -> maxHit = floor(maxHit * if (Random.nextDouble() < 0.05) 2.0 else 1.25)
                weapon.name.startsWith("dharoks_greataxe") && source.hasEffect("dharoks_set") -> {
                    val lost = source.levels.getMax(Skill.Constitution) - source.levels.get(Skill.Constitution) / 100.0
                    val max = source.levels.getMax(Skill.Constitution) / 100.0
                    maxHit = floor(maxHit * (1 + lost * max))
                }
                weapon.name.endsWith("vestas_longsword") -> maxHit = floor(maxHit * 1.2)
                weapon.name.endsWith("statiuss_warhammer") -> maxHit = floor(maxHit * 1.2)
            }
        }
        // DamageModifier Low
        if (source["in_castle_wars", false] && source.equipped(EquipSlot.Hands).name.startsWith("castle_wars_brace") && isFlagHolder(target)) {
            maxHit = floor(maxHit * 1.20)
        } else if (source.equipped(EquipSlot.Ring).name.startsWith("ferocious_ring")) {
            maxHit = floor(maxHit * 1.04)
        } else if (target is NPC && target.name == "ice_strykewyrm" && skill == Skill.Magic) {
            if (source.equipped(EquipSlot.Cape).name == "fire_cape") {
                maxHit += 40
            }
            if (source["spell", ""].startsWith("fire_")) {
                maxHit = floor(maxHit * if (source.equipped(EquipSlot.Cape).name == "fire_cape") 2.0 else 1.5)
            }
        }
    }
    if (target is NPC && target.def.has("damage_cap")) {
        maxHit = maxHit.coerceAtMost(target.def.get<Int>("damage_cap").toDouble())
    }
    return maxHit
}

fun usingSalamander(source: Character, skill: Skill): Boolean = source is Player && skill == Skill.Magic && !source.contains("spell") && source.equipped(EquipSlot.Weapon).name.endsWith("salamander")

fun getStrengthBonus(source: Character, skill: Skill): Int {
    return if (usingSalamander(source, skill)) {
        when ((source as Player).equipped(EquipSlot.Weapon).name) {
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

fun getMaximumHit(source: Character, target: Character? = null, skill: Skill): Int {
    val strengthBonus = getStrengthBonus(source, skill) + 64
    var maxHit = if (skill == Skill.Magic && !usingSalamander(source, skill)) {
        source["spell_damage", 0.0]
    } else {
        0.5 + (getEffectiveLevel(source, skill, accuracy = false) * strengthBonus) / 640
    }
    maxHit = applyBaseDamage(source, target, skill, maxHit, strengthBonus)
    maxHit = applyProtectionPrayer(source, target, skill, maxHit)
    maxHit = applyDamageModifier(source, target, skill, maxHit)
    return maxHit.toInt()
}

fun applyProtectionPrayer(source: Character, target: Character?, skill: Skill, maxHit: Double): Double {
    if (target == null) {
        return maxHit
    }
    if (skill == Skill.Strength && (target.hasEffect("prayer_protect_from_melee") || target.hasEffect("prayer_deflect_melee")) ||
        skill == Skill.Range && (target.hasEffect("prayer_protect_from_missiles") || target.hasEffect("deflect_missiles")) ||
        skill == Skill.Magic && (target.hasEffect("prayer_protect_from_magic") || target.hasEffect("deflect_magic")) ||
        isFamiliar(source) && (target.hasEffect("prayer_protect_from_summoning") || target.hasEffect("deflect_summoning"))
    ) {
        return floor(maxHit * if (source is Player && target is Player) 0.6 else 0.0)
    }
    return maxHit
}

fun isWeaponOutlier(special: Boolean, name: String): Boolean = (special && name.startsWith("magic") || name == "seercull" || name == "rune_thrownaxe") || name == "ogre_bow"
fun isTzhaarWeapon(weapon: Item) = weapon.name == "toktz-xil-ak" || weapon.name == "tzhaar-ket-om" || weapon.name == "tzhaar-ket-em" || weapon.name == "toktz-xil-ek"
fun isFlagHolder(target: Character?): Boolean = target is Player && (target.equipped(EquipSlot.Weapon).name == "zamorak_flag" || target.equipped(EquipSlot.Weapon).name == "saradomin_flag")
fun isKalphite(target: Character?): Boolean = target != null
fun isShade(target: Character?): Boolean = target != null
fun isFamiliar(target: Character?): Boolean = target != null && target is NPC
fun isDemon(target: Character?): Boolean = target != null
fun isUndead(target: Character?): Boolean = target != null && target.isUndead

// TODO other staves and npcs
fun isFirey(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_fire"
fun isWatery(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).name == "staff_of_water"

fun getEffectiveLevel(source: Character, skill: Skill, accuracy: Boolean): Int {
    var level = source.levels.get(skill).toDouble()
    level = applyBonus(source, level, skill)
    level += getStanceBonus(source, skill)
    level += 8.0
    // EffectiveLevelModifier High
    return applyVoidMultiplier(source, level, skill, accuracy).toInt()
}

fun getStanceBonus(source: Character, skill: Skill): Int {
    if ((skill == Skill.Attack || skill == Skill.Range) && source.attackStyle == "accurate") {
        return 3
    } else if ((skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence) && source.attackStyle == "controlled") {
        return 1
    } else if (skill == Skill.Defence && (source.attackStyle == "defensive" || source.attackStyle == "long_range")) {
        return 3
    } else if (skill == Skill.Strength && source.attackStyle == "aggressive") {
        return 3
    }
    return 0
}

fun applyBonus(source: Character, level: Double, skill: Skill): Double {
    return floor(level * source.getBonus("${skill.name.toLowerCase()}_bonus"))
}

fun applyVoidMultiplier(source: Character, level: Double, skill: Skill, accuracy: Boolean): Double {
    if (source !is Player) {
        return level
    }
    if (!source.hasEffect("void_set") && !source.hasEffect("elite_void_set")) {
        return level
    }
    if (accuracy && skill == Skill.Magic && source.equipped(EquipSlot.Hat).name == "void_mage_helm") {
        return floor(level * 1.3)
    }
    return floor(level * 1.1)
}

fun Player.getChance(target: Character?, skill: Skill, offense: Boolean): Int {
    val style = if (skill == Skill.Range) "range" else if (skill == Skill.Magic) "magic" else combatStyle
    val equipmentBonus = get(if (offense) style else "${style}_def", 0)
    var effective = if (target == null) 8 else getEffectiveLevel(this, if (skill == Skill.Magic && offense && target is Player) Skill.Defence else skill, offense)
    // EffectiveOverride
    if (skill == Skill.Magic && !offense) {
        if (target is NPC) {
            effective = target.levels.get(Skill.Magic) // High
        } else if (target is Player) {
            var level = floor(target.levels.get(Skill.Magic) * target.magicBonus)
            level = floor(level * 0.7)
            effective = (floor(effective * 0.3) + level).toInt() // Low
        }
    }
    var chance = effective * (equipmentBonus + 64.0)
    // HitChanceModifier
    if (offense) {
        chance = floor(chance * getSlayerMultiplier(skill, target, false))

        val weapon = equipped(EquipSlot.Weapon)
        val special = getVar("special_attack", false)

        if (special && skill == Skill.Attack) {
            when {
                weapon.name == "abyssal_whip" || weapon.name == "dragon_mace" || weapon.name == "dragon_scimitar" -> chance = floor(chance * 1.25)
                weapon.name.endsWith("godsword") || weapon.name == "barrelchest_anchor" || weapon.name == "saradomin_sword" -> chance = floor(chance * 2.0)
                weapon.name.startsWith("dragon_dagger") || weapon.name == "rune_claws" -> chance = floor(chance * 1.15)
            }
        }
    }
    return chance.toInt()
}

fun Player.hitChance(target: Character?, skill: Skill): Double {
    val attackerChance = getChance(this, skill, true)
    val defenderChance = getChance(target, skill, false)
    return if (attackerChance > defenderChance) {
        1.0 - (defenderChance + 2.0) / (2.0 * (attackerChance + 1.0))
    } else {
        attackerChance / (2.0 * (defenderChance + 1.0))
    }
}


fun Player.getSlayerMultiplier(skill: Skill, target: Character?, damage: Boolean): Double {
    if (!hasSlayerTask || !isTask(target)) {
        return 1.0
    }
    val helm = equipped(EquipSlot.Hat).name
    if (skill == Skill.Strength) {
        val amulet = equipped(EquipSlot.Amulet).name
        if (amulet == "salve_amulet_e") {
            return 1.2
        }
        if (amulet == "salve_amulet" || helm.startsWith("black_mask") || helm.startsWith("slayer_helmet")) {
            return 1.15
        }
    }

    if (skill == Skill.Range && (helm == "focus_sight" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    if (damage && skill == Skill.Magic && (helm == "hexcrest" || helm.startsWith("full_slayer_helmet"))) {
        return 1.15
    }
    return 1.0
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

var Character.attackBonus: Double
    get() = getBonus("attack_bonus")
    set(value) = setBonus("attack_bonus", value)

var Character.strengthBonus: Double
    get() = getBonus("strength_bonus")
    set(value) = setBonus("strength_bonus", value)

var Character.defenceBonus: Double
    get() = getBonus("defence_bonus")
    set(value) = setBonus("defence_bonus", value)

var Character.rangeBonus: Double
    get() = getBonus("range_bonus")
    set(value) = setBonus("range_bonus", value)

var Character.magicBonus: Double
    get() = getBonus("magic_bonus")
    set(value) = setBonus("magic_bonus", value)

private fun Character.getBonus(key: String): Double {
    return if (this is Player) {
        1.0 + (getVar(key, 30) - 30) / 100.0
    } else {
        get(key, 1.0)
    }
}

private fun Character.setBonus(key: String, value: Double) {
    if (this is Player) {
        setVar(key, (value - 1.0 * 100 + 30).toInt(), refresh = getVar("leech"))
    } else {
        set(key, value)
    }
}