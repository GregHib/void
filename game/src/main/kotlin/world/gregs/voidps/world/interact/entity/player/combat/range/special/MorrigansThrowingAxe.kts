package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.floor

fun isThrowingAxe(weapon: Item) = weapon.id.endsWith("morrigans_throwing_axe")

on<HitDamageModifier>({ type == "range" && special && isThrowingAxe(weapon) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.2)
}

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isThrowingAxe(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player.setAnimation("throw_morrigans_throwing_axe_special")
    player.setGraphic("${ammo}_special")
    player.shoot(id = ammo, target = target, height = 15)
    val distance = player.tile.distanceTo(target)
    if (player.hit(target, delay = Hit.throwDelay(distance)) != -1) {
        target.start("hamstring", 100)
    }
}