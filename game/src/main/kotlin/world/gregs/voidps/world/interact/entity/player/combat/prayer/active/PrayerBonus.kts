package world.gregs.voidps.world.interact.entity.player.combat.prayer.active

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.prayer.Prayer
import world.gregs.voidps.world.interact.entity.player.combat.prayer.prayerStart
import world.gregs.voidps.world.interact.entity.player.combat.prayer.prayerStop

fun set(name: String, bonus: String, value: Int) {
    prayerStart(name) { player ->
        player["base_${bonus}"] = player["base_${bonus}", 1.0] + value / 100.0
    }
    prayerStop(name) { player ->
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
set("sharp_eye", "ranged_attack_bonus", 5)
set("sharp_eye", "ranged_strength_bonus", 5)
set("hawk_eye", "ranged_attack_bonus", 10)
set("hawk_eye", "ranged_strength_bonus", 10)
set("eagle_eye", "ranged_attack_bonus", 15)
set("eagle_eye", "ranged_strength_bonus", 15)
set("rigour", "ranged_attack_bonus", 20)
set("rigour", "ranged_strength_bonus", 23)
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
set("leech_ranged", "ranged_attack_bonus", 5)
set("leech_ranged", "ranged_strength_bonus", 5)
set("leech_magic", "magic_bonus", 5)
set("leech_defence", "defence_bonus", 5)
set("leech_strength", "strength_bonus", 5)
set("turmoil", "attack_bonus", 15)
set("turmoil", "strength_bonus", 23)
set("turmoil", "defence_bonus", 15)

block(Priority.MEDIUM) { character ->
    if (!Prayer.usingDeflectPrayer(character, target, type)) {
        return@block
    }
    val damage = target["protected_damage", 0]
    if (damage > 0) {
        target.setAnimation("deflect", delay)
        target.setGraphic("deflect_${if (type == "melee") "attack" else type}", delay)
        if (random.nextDouble() >= 0.4) {
            target.hit(target = character, type = "deflect", delay = CLIENT_TICKS.toTicks(delay), damage = (damage * 0.10).toInt())
        }
        blocked = true
    }
}