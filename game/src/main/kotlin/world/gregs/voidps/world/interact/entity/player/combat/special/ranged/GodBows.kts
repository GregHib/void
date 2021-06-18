import kotlinx.coroutines.Job
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.Math
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.floor

fun isGodBow(weapon: Item?) = weapon != null && (weapon.name == "saradomin_bow" || weapon.name == "guthix_bow" || weapon.name == "zamorak_bow")

on<CombatSwing>({ player -> player.specialAttack && isGodBow(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, 550)) {
        return@on
    }
    player.setAnimation("bow_shoot")
    val ammo = player.ammo
    player.setGraphic("${ammo}_shoot", height = 100)
    player.shoot(name = ammo, target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
}

on<HitDamageModifier>({ type == "range" && weapon?.name == "guthix_bow" && it.specialAttack }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.5)
}

on<CombatHit>({ source is Player && isGodBow(weapon) && special }) { character: Character ->
    source as Player
    character.setGraphic("${weapon!!.name}_special_hit")
    source.playSound("god_bow_special_hit")
    when (weapon.name) {
        "zamorak_bow" -> hit(source, character, damage, type, weapon, special)
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
    player["restorative_job"] = delay(player, 10, true) {
        val amount = player["restoration", 0]
        if(amount <= 0) {
            player.stop(effect)
            return@delay
        }
        val restore = Math.interpolate(amount, 10, 60, 1, 380).coerceAtMost(amount)
        player["restoration"] = amount - restore
        player.levels.boost(Skill.Constitution, restore)
        player.setGraphic("saradomin_bow_restoration")
    }
}

on<EffectStop>({ effect == "restorative_shot" }) { player: Player ->
    player.remove<Job>("restorative_job")?.cancel()
    player.clear("restoration")
}