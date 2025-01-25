package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot

specialAttack("hamstring") { player ->
    val ammo = player.ammo
    player.anim("throw_morrigans_throwing_axe_special")
    player.gfx("${ammo}_special")
    val time = player.shoot(id = ammo, target = target, height = 15)
    if (player.hit(target, delay = time) != -1) {
        target.start(id, 100)
    }
}