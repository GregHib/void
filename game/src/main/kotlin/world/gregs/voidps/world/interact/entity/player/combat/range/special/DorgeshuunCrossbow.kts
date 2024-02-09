package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot

specialAttackSwing("dorgeshuun_crossbow", style = "range", priority = Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("crossbow_accurate")
    player.shoot(id = "bone_bolts_spec", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}