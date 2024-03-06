package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.target
import world.gregs.voidps.world.interact.entity.combat.underAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackPrepare

specialAttackPrepare("quick_smash") { player ->
    if (!player.underAttack) {
        return@specialAttackPrepare
    }
    cancel()
    if (!SpecialAttack.drain(player)) {
        return@specialAttackPrepare
    }
    val target = player.target ?: return@specialAttackPrepare
    player.setAnimation(id)
    player.setGraphic(id)
    player.hit(target)
}