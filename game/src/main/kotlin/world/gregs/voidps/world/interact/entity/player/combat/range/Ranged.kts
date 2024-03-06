package world.gregs.voidps.world.interact.entity.player.combat.range

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

val weaponStyles: WeaponStyleDefinitions by inject()

combatPrepare("range") { player ->
    if (player.specialAttack && !drainSpecialEnergy(player)) {
        cancel()
    }
}

combatSwing(style = "range") { player ->
    val ammo = player.ammo
    val required = Ammo.requiredAmount(player.weapon, player.specialAttack)
    val style = weaponStyles.get(player.weapon.def["weapon_style", 0])
    if (SpecialAttack.drain(player)) {
        val id: String = player.weapon.def.getOrNull("special") ?: return@combatSwing
        player.emit(SpecialAttack(id, target))
        return@combatSwing
    }
    var time = -1
    when (style.stringId) {
        "chinchompa" -> {
            Ammo.remove(player, target, ammo, required)
            player.setAnimation("chinchompa_short_fuse")
            player.shoot(id = ammo, target = target)
        }
        "pie" -> {
            if (!player.equipment.remove(ammo, required)) {
                cancel()
                return@combatSwing
            }
            player.ammo = ""
            player.setAnimation("mud_pie")
            time = player.shoot(id = ammo, target = target)
        }
        "thrown" -> {
            Ammo.remove(player, target, ammo, required)
            val ammoName = player.ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
            player.setAnimation(when {
                ammoName.contains("_javelin") -> "throw_javelin"
                ammoName.contains("_dart") -> "throw_dart"
                ammoName.contains("_knife") -> "thrown_accurate"
                ammoName.contains("_throwing_axe") -> if (ammoName.contains("morrigans")) "throw_javelin" else "thrown_accurate"
                else -> ammoName
            })
            player.setGraphic("${ammoName}_throw")
            time = player.shoot(id = ammo, target = target)
        }
        "bow" -> {
            Ammo.remove(player, target, ammo, required)
            player.setAnimation("bow_accurate")
            player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
            time = player.shoot(id = if (ammo.endsWith("brutal")) "brutal_arrow" else ammo, target = target)
        }
        "crossbow" -> {
            Ammo.remove(player, target, ammo, required)
            player.setAnimation(if (player.weapon.id == "karils_crossbow") "karils_crossbow_shoot" else "crossbow_accurate")
            val bolt = if (ammo == "barbed_bolts" || ammo == "bone_bolts" || ammo == "hand_cannon_shot") ammo else "crossbow_bolt"
            time = player.shoot(id = bolt, target = target)
        }
        "fixed_device" -> {
            Ammo.remove(player, target, ammo, required)
            // TODO
        }
        "salamander" -> {
            Ammo.remove(player, target, ammo, required)
            // TODO
        }
        "sling" -> {
            player.setAnimation(ammo)
            time = player.shoot(id = ammo, target = target)
        }
    }
    player.hit(target, delay = if (time == -1) 64 else time)
}