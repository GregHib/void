package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.underAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackPrepare

combatSwing("granite_maul*", "melee", special = true)  { player ->
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash")
    player.hit(target)
}

specialAttackPrepare("granite_maul*") { player ->
    if (!player.underAttack) {
        return@specialAttackPrepare
    }
    val target: Character? = player["target"]
    if (target == null) {
        player.specialAttack = false
        return@specialAttackPrepare
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        return@specialAttackPrepare
    }
    player.setAnimation("quick_smash")
    player.setGraphic("quick_smash")
    player.hit(target)
    player.specialAttack = false
}