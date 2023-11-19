package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.nextInt

fun isCrossbow(weapon: Item?) = weapon != null && weapon.id == "zaniks_crossbow"

fun hasActivePrayer(player: Player): Boolean {
    return (player.variables as PlayerVariables).temp.any { (key, value) -> key.startsWith("prayer_") && value == true }
}

fun hasGodArmour(player: Player) = false

on<HitDamageModifier>({ type == "range" && special && weapon?.id == "zaniks_crossbow" }, Priority.HIGH) { _: Player ->
    if (target is NPC) {
        damage += random.nextInt(30..150)
    } else if (target is Player && (hasActivePrayer(target) || hasGodArmour(target))) {
        damage += random.nextInt(0..150)
    }
}

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isCrossbow(player.weapon) }, Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, 500)) {
        delay = -1
        return@on
    }
    player.setAnimation("zaniks_crossbow_special")
    player.setGraphic("zaniks_crossbow_special")
    player.shoot(id = "zaniks_crossbow_bolt", target = target)
    val distance = player.tile.distanceTo(target)
    val damage = player.hit(target, delay = Hit.bowDelay(distance))
    if (damage != -1) {
        target.levels.drain(Skill.Defence, damage / 10)
    }
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}