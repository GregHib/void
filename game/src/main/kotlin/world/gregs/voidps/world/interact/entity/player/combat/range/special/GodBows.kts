package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.bowHitDelay
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit
import kotlin.math.floor

fun isGodBow(weapon: Item?) = weapon != null && (weapon.id == "saradomin_bow" || weapon.id == "guthix_bow" || weapon.id == "zamorak_bow")

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isGodBow(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_shoot")
    val ammo = player.ammo
    player.setGraphic("${ammo}_shoot")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = bowHitDelay(distance))
}

on<HitDamageModifier>({ type == "range" && weapon?.id == "guthix_bow" && special }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.5)
}

var Player.restoration: Int
    get() = this["restoration", 0]
    set(value) {
        this["restoration"] = value
    }

on<CombatHit>({ source is Player && isGodBow(weapon) && special }) { character: Character ->
    source as Player
    character.setGraphic("${weapon!!.id}_special_hit")
    source.playSound("god_bow_special_hit")
    when (weapon.id) {
        "zamorak_bow" -> hit(source, character, damage, type, weapon, spell, special)
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