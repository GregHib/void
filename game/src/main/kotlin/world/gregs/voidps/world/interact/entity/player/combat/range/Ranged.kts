package world.gregs.voidps.world.interact.entity.player.combat.range

import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

val weaponStyles: WeaponStyleDefinitions by inject()
val animationDefinitions: WeaponAnimationDefinitions by inject()

combatPrepare("range") { player ->
    if (player.specialAttack && !drainSpecialEnergy(player)) {
        cancel()
    }
}

combatSwing(style = "range") { player ->
    var ammo = player.ammo
    val required = Ammo.requiredAmount(player.weapon, player.specialAttack)
    if (SpecialAttack.drain(player)) {
        val id: String = player.weapon.def.getOrNull("special") ?: return@combatSwing
        player.emit(SpecialAttack(id, target))
        return@combatSwing
    }
    val style = weaponStyles.get(player.weapon.def["weapon_style", 0])
    when (style.stringId) {
        "thrown" -> {
            val ammoName = player.ammo.removePrefix("corrupt_").removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
            player.setGraphic("${ammoName}_throw")
        }
        "bow" -> {
            player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
        }
        "fixed_device" -> {
            // TODO
        }
        "salamander" -> {
            // TODO
        }
    }
    if (style.stringId != "sling") {
        Ammo.remove(player, target, ammo, required)
    } else {
        player.setAnimation(ammo)
    }
    if (style.stringId == "crossbow") {
        ammo = if (ammo == "barbed_bolts" || ammo == "bone_bolts" || ammo == "hand_cannon_shot") ammo else "crossbow_bolt"
    } else if(style.stringId == "bow" && ammo.endsWith("brutal")) {
        ammo = "brutal_arrow"
    }
    val type: String? = player.weapon.def.getOrNull("weapon_type") ?: style.stringId
    val definition = if (type != null) animationDefinitions.get(type) else null
    var animation = definition?.attackTypes?.getOrDefault(player.attackType, definition.attackTypes["default"])
    if (animation == null) {
        animation = "${style.stringId}_${player.attackType}"
    }
    val time = player.shoot(id = ammo, target = target)
    player.setAnimation(animation)
    player.hit(target, delay = if (time == -1) 64 else time)
}