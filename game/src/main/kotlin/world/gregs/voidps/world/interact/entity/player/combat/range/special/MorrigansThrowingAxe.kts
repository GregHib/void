package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.floor

fun isThrowingAxe(weapon: Item?) = weapon != null && (weapon.name.endsWith("morrigans_throwing_axe"))

on<HitDamageModifier>({ type == "range" && special && isThrowingAxe(weapon) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.2)
}

on<CombatSwing>({ player -> !swung() && player.specialAttack && isThrowingAxe(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player.setAnimation("throw_morrigans_throwing_axe_special")
    player.setGraphic("${ammo}_special")
    player.shoot(name = ammo, target = target, height = 15)
    if (player.hit(target) != -1) {
        target.start("hamstring", 100)
    }
}