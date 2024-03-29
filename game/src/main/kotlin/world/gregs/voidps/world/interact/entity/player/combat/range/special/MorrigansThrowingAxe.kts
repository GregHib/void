package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

specialAttack("hamstring") { player ->
    val ammo = player.ammo
    player.setAnimation("throw_morrigans_throwing_axe_special")
    player.setGraphic("${ammo}_special")
    val time = player.shoot(id = ammo, target = target, height = 15)
    if (player.hit(target, delay = time) != -1) {
        target.start(id, 100)
    }
}