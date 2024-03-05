package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

combatSwing("*_knife*", "range") { player ->
    val required = player.weapon.def["ammo_required", 1]
    var ammo = player.weapon.id
    player.ammo = ""
    Ammo.remove(player, target, ammo, required)
    player.ammo = ammo
    if (!player.specialAttack) {
        ammo = player.ammo.removeSuffix("_p++").removeSuffix("_p+").removeSuffix("_p")
        player.setAnimation("thrown_accurate")
        player.setGraphic("${ammo}_throw")
        player.shoot(id = ammo, target = target)
        val distance = player.tile.distanceTo(target)
        player.hit(target, delay = Hit.throwDelay(distance))
        delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
    }
}