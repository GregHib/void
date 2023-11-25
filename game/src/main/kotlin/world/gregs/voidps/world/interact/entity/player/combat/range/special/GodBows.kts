package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

fun isGodBow(weapon: Item) = weapon.id == "saradomin_bow" || weapon.id == "guthix_bow" || weapon.id == "zamorak_bow"

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isGodBow(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${ammo}_shoot")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
}

var Player.restoration: Int
    get() = this["restoration", 0]
    set(value) {
        this["restoration"] = value
    }

on<CombatAttack>({ isGodBow(weapon) && special }) { source: Player ->
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

on<CombatHit>({ source is Player && isGodBow(weapon) && special }) { character: Character ->
    source as Player
    character.setGraphic("${weapon.id}_special_hit")
    source.playSound("god_bow_special_hit")
}

on<TimerStart>({ timer == "restorative_shot" || timer == "balanced_shot" }) { _: Player ->
    interval = TimeUnit.SECONDS.toTicks(6)
}

on<TimerTick>({ timer == "restorative_shot" || timer == "balanced_shot" }) { player: Player ->
    val amount = player.restoration
    if (amount <= 0) {
        return@on cancel()
    }
    val restore = player["restoration_amount", 0]
    player.restoration -= restore
    player.levels.restore(Skill.Constitution, restore)
    player.setGraphic("saradomin_bow_restoration")
}

on<TimerStop>({ timer == "restorative_shot" || timer == "balanced_shot" }) { player: Player ->
    player.clear("restoration")
    player.clear("restoration_amount")
}