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


fun Player.gearBonus(target: Character): Double {
    var gearBonus = 1.0
    if (hasSlayerTask && isTask(target) && target.isUndead && equipped(EquipSlot.Amulet).name == "salve_amulet_e") {
        gearBonus *= 7 / 6
    } else if (hasSlayerTask && isTask(target) && equipped(EquipSlot.Hat).name.startsWith("full_slayer_helmet")) {
        gearBonus *= 1.15
    }
    return gearBonus
}

fun Player.maximumRangedHit(target: Character): Int {
    val exception = true/*
    special attacks
        * Magic longbow
        * Magic shortbow
        * Magic composite bow
        * Seercull
        * Rune thrownaxe
    normal attack
        * Ogre bow s
*/
    val equipmentStrength = get("range_str", 0)
    if (exception) {
        val extras = 0// +1 if using rune thrownaxe or magic shortbow in pvp
        return (0.5 + (levels.get(Skill.Range) + 10) * (equipmentStrength + 64) / 640).toInt() + extras
    } else {
        val specialAttackMultiplier = 1.0// spec + diamond bolts = onyx
        val prayerMultiplier = 1.0
        val boltSpecMultiplier = 1.0// dragon e, pearl + opal
        val antiFire = 1.0// 0.0 if immune
        return ((0.5 + (effectiveAttack * (equipmentStrength + 64)) / 640) * gearBonus(target) * specialAttackMultiplier * prayerMultiplier * boltSpecMultiplier * antiFire).toInt()
    }
}

val Player.effectiveAttack: Double
    get() {
        val accurate = if (attackStyle == 0) 3 else 0
        return (levels.get(Skill.Range) + accurate + 8.0) * voidMultiplier()
    }

fun Player.effectiveDefLevel(target: Character): Int {
    return if (target is Player) {
        target.levels.get(Skill.Defence) + if (attackStyle == 2) 3 else 1// todo 3 if def, 1 if control, 0 else
    } else {
        target.levels.get(Skill.Defence) + 9
    }
}

fun Player.attackChance(target: Character): Double {
    val gearBonus = gearBonus(target)
    val rangedBonus = get("range", 0)
    val specialAttackMultiplier = 1.0
    return effectiveAttack * (rangedBonus + 64) * gearBonus * specialAttackMultiplier
}

fun Player.defenceChance(target: Character): Int {
    val rangeBonusTarget = target["range_def", 0]
    return effectiveDefLevel(target) * (rangeBonusTarget + 64)
}

fun Player.voidMultiplier(): Double {
    return when {
        wearingVoid && equipped(EquipSlot.Hat).name == "void_ranger_helm" -> 1.1
        wearingEliteVoid && equipped(EquipSlot.Hat).name == "void_ranger_helm" -> 1.125
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

val Player.wearingVoid: Boolean
    get() = equipped(EquipSlot.Chest).name == "void_knight_top" &&
            equipped(EquipSlot.Legs).name == "void_knight_robe" &&
            equipped(EquipSlot.Hands).name == "void_knight_gloves"

val Player.wearingEliteVoid: Boolean
    get() = equipped(EquipSlot.Chest).name.startsWith("elite_void_knight_top") &&
            equipped(EquipSlot.Legs).name.startsWith("elite_void_knight_robe") &&
            equipped(EquipSlot.Hands).name.startsWith("elite_void_knight_gloves")

val ItemDefinition.ammo: Set<String>?
    get() = (getOrNull("ammo") as? ArrayList<String>)?.toSet()

val Player.attackStyle: Int
    get() = getVar("attack_style")