package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.stopTimer
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.bowHitDelay
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
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

on<CombatHit>({ source is Player && isGodBow(weapon) && special }) { character: Character ->
    source as Player
    character.setGraphic("${weapon!!.id}_special_hit")
    source.playSound("god_bow_special_hit")
    when (weapon.id) {
        "zamorak_bow" -> hit(source, character, damage, type, weapon, spell, special)
        "saradomin_bow" -> {
            val restore = source["restoration", 0]
            source.start("restorative_shot")
            source["restoration"] = restore + (damage * 2)
        }
        "guthix_bow" -> {
            val restore = source["restoration", 0]
            source.start("balanced_shot")
            source["restoration"] = restore + (damage * 1.5).toInt()
        }
    }
}

on<EffectStart>({ effect == "restorative_shot" }) { player: Player ->
    player.timer("restorative", 10) {
        val amount = player["restoration", 0]
        if(amount <= 0) {
            player.stop(effect)
            return@timer
        }
        val restore = Interpolation.interpolate(amount, 10, 60, 1, 380).coerceAtMost(amount)
        player["restoration"] = amount - restore
        player.levels.restore(Skill.Constitution, restore)
        player.setGraphic("saradomin_bow_restoration")
    }
}

on<EffectStop>({ effect == "restorative_shot" }) { player: Player ->
    player.stopTimer("restorative")
    player.clear("restoration")
}