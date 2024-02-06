package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot

weaponSwing("*sling", style = "range", priority = Priority.LOW) { player: Player ->
    val ammo = player.ammo
    player.setAnimation(ammo)
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.throwDelay(distance))
    delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
}