package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.world.activity.skill.slayer.hasSlayerTask
import world.gregs.voidps.world.activity.skill.slayer.isTask
import world.gregs.voidps.world.activity.skill.slayer.isUndead
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") as? Int ?: ShootProjectile.DEFAULT_HEIGHT

fun rangeHit(player: Player, target: Character, damage: Int = Random.nextInt(100 + 1).coerceAtLeast(0)) {
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


fun Player.slayGearBonus(target: Character): Double {
    if (!hasSlayerTask || !isTask(target)) {
        return 1.0
    }
    return when {
        target.isUndead && equipped(EquipSlot.Amulet).name == "salve_amulet_e" -> 7.0 / 6.0
        equipped(EquipSlot.Hat).name.startsWith("full_slayer_helmet") -> 1.15
        else -> 1.0
    }
}

fun isWeaponOutlier(special: Boolean, name: String): Boolean =
    (special && name.startsWith("magic") || name == "seercull" || name == "rune_thrownaxe") || name == "ogre_bow"

fun Player.maximumRangedHit(target: Character): Int {
    val weapon = equipped(EquipSlot.Weapon)
    val special = getVar("special_attack", false)
    val equipmentStrength = get("range_str", 0)
    return if (isWeaponOutlier(special, weapon.name)) {
        val bonus = if (weapon.name == "rune_thrownaxe" || (weapon.name == "magic_short_bow" && target is Player)) 1 else 0
        (0.5 + (levels.get(Skill.Range) + 10) * (equipmentStrength + 64) / 640).toInt() + bonus
    } else {
        val specialAttackMultiplier = 1.0// spec + diamond bolts = onyx
        val prayerMultiplier = 1.0
        val boltSpecMultiplier = 1.0// dragon e, pearl + opal
        val antiFire = 1.0// 0.0 if immune
        ((0.5 + (effectiveAttack * (equipmentStrength + 64)) / 640) * slayGearBonus(target) * specialAttackMultiplier * prayerMultiplier * boltSpecMultiplier * antiFire).toInt()
    }
}

val Player.effectiveAttack: Double
    get() {
        val accurate = if (attackStyle == "Accurate") 3 else 0
        val prayerMultiplier = 1.0
        return (levels.get(Skill.Range) * prayerMultiplier + accurate + 8.0) * voidMultiplier()
    }

fun Player.effectiveDefLevel(target: Character): Int {
    return target.levels.get(Skill.Defence) + if (target is Player) if (attackStyle == "Defensive") 3 else if (attackStyle == "Controlled") 1 else 0 else 9
}

fun Player.attackChance(target: Character): Double {
    val gearBonus = slayGearBonus(target)
    val rangedBonus = get("range", 0)
    val specialAttackMultiplier = 1.0
    return effectiveAttack * (rangedBonus + 64) * gearBonus * specialAttackMultiplier
}

fun Player.defenceChance(target: Character): Int {
    val rangeBonusTarget = target["range_def", 0]
    return effectiveDefLevel(target) * (rangeBonusTarget + 64)
}

fun Player.voidMultiplier(): Double {
    if (equipped(EquipSlot.Hat).name != "void_ranger_helm") {
        return 1.0
    }
    return when {
        hasEffect("void_set") -> 1.1
        hasEffect("elite_void_set") -> 1.125
        else -> 1.0
    }
}

fun Player.hitChance(target: Character): Double {
    val attackerChance = attackChance(target)
    val defenderChance = defenceChance(target)
    return if (attackerChance > defenderChance) {
        1 - (defenderChance + 2) / (2 * (attackerChance + 1))
    } else {
        attackerChance / (2 * (defenderChance + 1))
    }
}

val ItemDefinition.ammo: Set<String>?
    get() = (getOrNull("ammo") as? ArrayList<String>)?.toSet()

val Player.attackStyle: String
    get() = get("attack_style")

val Player.attackType: String
    get() = get("attack_type")