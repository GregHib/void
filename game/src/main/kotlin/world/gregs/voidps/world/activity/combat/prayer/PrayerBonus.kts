package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import kotlin.math.floor

fun set(name: String, bonus: String, value: Int) {
    on<PrayerStart>({ this.prayer == name }) { player: Player ->
        player["base_${bonus}"] = player["base_${bonus}", 1.0] + value / 100.0
    }
    on<PrayerStop>({ this.prayer == name }) { player: Player ->
        player["base_${bonus}"] = player["base_${bonus}", 1.0] - value / 100.0
    }
}

set("clarity_of_thought", "attack_bonus", 5)
set("improved_reflexes", "attack_bonus", 10)
set("incredible_reflexes", "attack_bonus", 15)
set("chivalry", "attack_bonus", 15)
set("chivalry", "strength_bonus", 18)
set("chivalry", "defence_bonus", 20)
set("piety", "attack_bonus", 20)
set("piety", "strength_bonus", 23)
set("piety", "defence_bonus", 25)
set("sharp_eye", "ranged_bonus", 5)
set("hawk_eye", "ranged_bonus", 10)
set("eagle_eye", "ranged_bonus", 15)
set("rigour", "ranged_bonus", 23)
set("rigour", "defence_bonus", 25)
set("mystic_will", "magic_bonus", 5)
set("mystic_lore", "magic_bonus", 10)
set("mystic_might", "magic_bonus", 15)
set("augury", "magic_bonus", 25)
set("augury", "defence_bonus", 25)
set("thick_skin", "defence_bonus", 5)
set("rock_skin", "defence_bonus", 10)
set("steel_skin", "defence_bonus", 15)
set("burst_of_strength", "strength_bonus", 5)
set("superhuman_strength", "strength_bonus", 10)
set("ultimate_strength", "strength_bonus", 15)
set("leech_attack", "attack_bonus", 5)
set("leech_ranged", "ranged_bonus", 5)
set("leech_magic", "magic_bonus", 5)
set("leech_defence", "defence_bonus", 5)
set("leech_strength", "strength_bonus", 5)
set("turmoil", "attack_bonus", 15)
set("turmoil", "strength_bonus", 23)
set("turmoil", "defence_bonus", 15)

fun usingProtectionPrayer(source: Character, target: Character?, type: String): Boolean {
    return target != null && (type == "melee" && (target.praying("protect_from_melee") || target.praying("deflect_melee")) ||
            type == "range" && (target.praying("protect_from_missiles") || target.praying("deflect_missiles")) ||
            type == "magic" && (target.praying("protect_from_magic") || target.praying("deflect_magic")) ||
            source.isFamiliar && (target.praying("protect_from_summoning") || target.praying("deflect_summoning")))
}

fun usingDeflectPrayer(source: Character, target: Character, type: String): Boolean {
    return (type == "melee" && target.praying("deflect_melee")) ||
            (type == "range" && target.praying("deflect_missiles")) ||
            (type == "magic" && target.praying("deflect_magic")) ||
            source.isFamiliar && (target.praying("deflect_summoning"))
}

fun hitThroughProtectionPrayer(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean): Boolean {
    if (target == null || weapon == null) {
        return false
    }
    if (special && weapon.id == "ancient_mace" && type == "melee") {
        return target.praying("protect_from_melee") || target.praying("deflect_melee")
    }
    return false
}

on<CombatAttack>({ !blocked && target is Player && usingDeflectPrayer(it, target, type) }, Priority.MEDIUM) { character: Character ->
    val damage = target["protected_damage", 0]
    if (damage > 0) {
        target.setAnimation("deflect", delay)
        target.setGraphic("deflect_${if (type == "melee") "attack" else type}", delay)
        if (random.nextDouble() >= 0.4) {
            target.hit(character, null, "deflect", delay, "", false, damage = (damage * 0.10).toInt())
        }
        blocked = true
    }
}

on<HitDamageModifier>(priority = Priority.HIGH) { _: Character ->
    target?.clear("protected_damage")
}

on<HitDamageModifier>({ usingProtectionPrayer(it, target, type) && !hitThroughProtectionPrayer(it, target, type, weapon, special) }, priority = Priority.MEDIUM) { _: Player ->
    target?.set("protected_damage", damage)
    damage = floor(damage * if (target is Player) 0.6 else 0.0)
}

on<HitDamageModifier>({ usingProtectionPrayer(it, target, type) }, priority = Priority.MEDIUM) { _: NPC ->
    target?.set("protected_damage", damage)
    damage = 0.0
}

on<HitEffectiveLevelModifier>(priority = Priority.HIGH) { player: Player ->
    var bonus = player["base_${skill.name.lowercase()}_bonus", 1.0]
    if (player.equipped(EquipSlot.Amulet).id == "amulet_of_zealots") {
        bonus = floor(1.0 + (bonus - 1.0) * 2)
    }
    bonus += if (player["turmoil", false]) {
        player["turmoil_${skill.name.lowercase()}_bonus", 0].toDouble() / 100.0
    } else {
        player.getLeech(skill) * 100.0 / player.levels.getMax(skill) / 100.0
    }
    bonus -= player.getBaseDrain(skill) + player.getDrain(skill) / 100.0
    level = floor(level * bonus)
}

on<HitEffectiveLevelModifier>(priority = Priority.HIGH) { npc: NPC ->
    val drain = 1.0 - ((npc.getBaseDrain(skill) + npc.getDrain(skill)) / 100.0)
    level = floor(level * drain)
}