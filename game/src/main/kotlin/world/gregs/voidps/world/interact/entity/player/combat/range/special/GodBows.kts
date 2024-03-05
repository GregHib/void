package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.*
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val swingHandler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@handler
    }
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${ammo}_shoot")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
}
combatSwing("saradomin_bow", style = "range", special = true, block = swingHandler)
combatSwing("guthix_bow", style = "range", special = true, block = swingHandler)
combatSwing("zamorak_bow", style = "range", special = true, block = swingHandler)

var Player.restoration: Int
    get() = this["restoration", 0]
    set(value) {
        this["restoration"] = value
    }

val specialHandler: suspend CombatAttack.(Player) -> Unit = combatAttack@{ source ->
    when (weapon.id) {
        "zamorak_bow" -> target.hit(source, weapon, type, CLIENT_TICKS.toTicks(delay), spell, special, damage)
        "saradomin_bow" -> {
            source.restoration += damage * 2
            source["restoration_amount"] = source.restoration / 10
            source.softTimers.start("restorative_shot")
        }
        "guthix_bow" -> {
            source.restoration += (damage * 1.5).toInt()
            source["restoration_amount"] = source.restoration / 10
            source.softTimers.start("balanced_shot")
        }
    }
}
combatAttack("saradomin_bow", special = true, block = specialHandler)
combatAttack("guthix_bow", special = true, block = specialHandler)
combatAttack("zamorak_bow", special = true, block = specialHandler)

val hitHandler: suspend CombatHit.(Character) -> Unit = { character ->
    character.setGraphic("${weapon.id}_special_hit")
    source.playSound("god_bow_special_hit")
}
combatHit("saradomin_bow", special = true, block = hitHandler)
combatHit("guthix_bow", special = true, block = hitHandler)
combatHit("zamorak_bow", special = true, block = hitHandler)

timerStart("restorative_shot", "balanced_shot") {
    interval = TimeUnit.SECONDS.toTicks(6)
}

timerTick("restorative_shot", "balanced_shot") { player ->
    val amount = player.restoration
    if (amount <= 0) {
        cancel()
        return@timerTick
    }
    val restore = player["restoration_amount", 0]
    player.restoration -= restore
    player.levels.restore(Skill.Constitution, restore)
    player.setGraphic("saradomin_bow_restoration")
}

timerStop("restorative_shot", "balanced_shot") { player ->
    player.clear("restoration")
    player.clear("restoration_amount")
}