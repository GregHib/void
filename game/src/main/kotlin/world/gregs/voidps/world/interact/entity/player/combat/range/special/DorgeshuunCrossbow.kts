package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isCrossbow(weapon: Item) = weapon.name == "dorgeshuun_crossbow"

on<CombatSwing>({ player -> !swung() && player.specialAttack && isCrossbow(player.weapon) }, Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@on
    }
    player.setAnimation("crossbow_shoot")
    player.shoot(name = "bone_bolts_spec", target = target, delay = 40, height = 43, endHeight = target.height, curve = 8)
    player.hit(target)
    val speed = player.weapon.def.getOrNull("attack_speed") as? Int ?: 4
    delay = if (player.attackType == "rapid") speed - 1 else speed
}