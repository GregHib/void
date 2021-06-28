package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import kotlin.math.floor

fun set(effect: String, bonus: String, value: Int) {
    on<EffectStart>({ this.effect == effect }) { player: Player ->
        player["base_${bonus}_bonus"] = player["base_${bonus}_bonus", 1.0] + value / 100.0
    }
    on<EffectStop>({ this.effect == effect }) { player: Player ->
        player["base_${bonus}_bonus"] = player["base_${bonus}_bonus", 1.0] - value / 100.0
    }
}

set("prayer_clarity_of_thought", "attack_bonus", 5)
set("prayer_improved_reflexes", "attack_bonus", 10)
set("prayer_incredible_reflexes", "attack_bonus", 15)
set("prayer_chivalry", "attack_bonus", 15)
set("prayer_chivalry", "strength_bonus", 18)
set("prayer_chivalry", "defence_bonus", 20)
set("prayer_piety", "attack_bonus", 20)
set("prayer_piety", "strength_bonus", 23)
set("prayer_piety", "defence_bonus", 25)
set("prayer_sharp_eye", "range_bonus", 5)
set("prayer_hawk_eye", "range_bonus", 10)
set("prayer_eagle_eye", "range_bonus", 15)
set("prayer_rigour", "range_bonus", 23)
set("prayer_rigour", "defence_bonus", 25)
set("prayer_mystic_will", "magic_bonus", 5)
set("prayer_mystic_lore", "magic_bonus", 10)
set("prayer_mystic_might", "magic_bonus", 15)
set("prayer_augury", "magic_bonus", 25)
set("prayer_augury", "defence_bonus", 25)
set("prayer_thick_skin", "defence_bonus", 5)
set("prayer_rock_skin", "defence_bonus", 10)
set("prayer_steel_skin", "defence_bonus", 15)
set("prayer_burst_of_strength", "strength_bonus", 5)
set("prayer_superhuman_strength", "strength_bonus", 10)
set("prayer_ultimate_strength", "strength_bonus", 15)
set("prayer_leech_attack", "attack_bonus", 5)
set("prayer_leech_ranged", "range_bonus", 5)
set("prayer_leech_magic", "magic_bonus", 5)
set("prayer_leech_defence", "defence_bonus", 5)
set("prayer_leech_strength", "strength_bonus", 5)
set("prayer_turmoil", "attack_bonus", 15)
set("prayer_turmoil", "strength_bonus", 23)
set("prayer_turmoil", "defence_bonus", 15)

fun usingProtectionPrayer(source: Character, target: Character?, type: String): Boolean {
    return target != null && (type == "melee" && (target.hasEffect("prayer_protect_from_melee") || target.hasEffect("prayer_deflect_melee")) ||
            type == "range" && (target.hasEffect("prayer_protect_from_missiles") || target.hasEffect("deflect_missiles")) ||
            type == "spell" && (target.hasEffect("prayer_protect_from_magic") || target.hasEffect("deflect_magic")) ||
            source.isFamiliar && (target.hasEffect("prayer_protect_from_summoning") || target.hasEffect("deflect_summoning")))
}

fun usingDeflectPrayer(source: Character, target: Character, type: String): Boolean {
    return (type == "melee" && target.hasEffect("prayer_deflect_melee")) ||
            (type == "range" && target.hasEffect("deflect_missiles")) ||
            (type == "spell" && target.hasEffect("deflect_magic")) ||
            source.isFamiliar && (target.hasEffect("deflect_summoning"))
}

on<CombatHit>({ usingDeflectPrayer(source, it, type) }) { player: Player ->
    player.setAnimation("deflect")
    player.setGraphic("deflect_${if (type == "spell") "magic" else if (type == "melee") "attack" else type}")
}

on<HitDamageModifier>({ usingProtectionPrayer(it, target, type) }, priority = Priority.MEDIUM) { _: Player ->
    damage = floor(damage * if (target is Player) 0.6 else 0.0)
}

on<HitDamageModifier>({ usingProtectionPrayer(it, target, type) }, priority = Priority.MEDIUM) { _: NPC ->
    damage = 0.0
}

on<HitEffectiveLevelModifier>(priority = Priority.HIGH) { player: Player ->
    var bonus = player["base_${skill.name.toLowerCase()}_bonus", 1.0]
    if (player.equipped(EquipSlot.Amulet).name == "amulet_of_zealots") {
        bonus = floor(1.0 + (bonus - 1.0) * 2)
    }
    if (player.getVar("turmoil", false)) {
        bonus += player.getVar("turmoil_${skill.name.toLowerCase()}_bonus", 0).toDouble()
    } else {
        bonus += player.getLeech(skill) * 100.0 / player.levels.getMax(skill) / 100.0
        bonus -= player.getDrain(skill) / 100.0
    }
    level = floor(level * bonus)
}

on<HitEffectiveLevelModifier>(priority = Priority.HIGH) { npc: NPC ->
    level = floor(level * npc["${skill.name.toLowerCase()}_bonus", 1.0])
}