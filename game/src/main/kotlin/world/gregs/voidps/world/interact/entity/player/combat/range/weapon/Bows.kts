package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    if (player.weapon.id.endsWith("crossbow")) {
        return@handler
    }
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${if (ammo.endsWith("brutal")) "brutal" else ammo}_shoot")
    player.shoot(id = if (ammo.endsWith("brutal")) "brutal_arrow" else ammo, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = 1 + (3 + distance) / 6)
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}
combatSwing("*bow", "range", block = handler)
combatSwing("seercull", "range", block = handler)
combatSwing("longbow_sighted", "range", block = handler)