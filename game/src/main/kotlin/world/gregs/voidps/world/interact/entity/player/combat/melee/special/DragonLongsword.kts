package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@handler
    }
    player.setAnimation("cleave")
    player.setGraphic("cleave")
    player.hit(target)
    delay = 5
}
combatSwing("dragon_longsword*", "melee", special = true, block = handler)
combatSwing("corrupt_dragon_longsword*", "melee", special = true, block = handler)