package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.target
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackPrepare

specialAttackPrepare("quick_smash") { player ->
    if (player.target == null) {
        return@specialAttackPrepare
    }
    cancel()
    if (!SpecialAttack.drain(player)) {
        return@specialAttackPrepare
    }
    val target = player.target ?: return@specialAttackPrepare
    player.anim("${id}_special")
    player.gfx("${id}_special")
    player.hit(target)
}